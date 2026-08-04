package com.tcc.sspsp.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.tcc.sspsp.model.Delegacias;
import com.tcc.sspsp.model.Ocorrencia;
import com.tcc.sspsp.repository.DelegaciasRepository;
import com.tcc.sspsp.repository.NaturezaRepository;

/**
 * Zero rede: toda chamada HTTP do {@link SSPClient} é redirecionada para um
 * servidor WireMock local via um {@link ClientHttpRequestFactory} customizado
 * que reescreve host/porta da requisição antes de executá-la.
 */
@ExtendWith(MockitoExtension.class)
class SSPClientTest {

	private static final String PATH = "/v1/OcorrenciasMensais/ExportarMensal";
	private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static final int ID_GRUPO = 5;

	@RegisterExtension
	static WireMockExtension wm = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort())
			.build();

	@Mock
	private NaturezaRepository naturezaRepository;

	@Mock
	private DelegaciasRepository delegaciasRepository;

	private SSPClient sspClient;

	@BeforeEach
	void setUp() {
		sspClient = new SSPClient(naturezaRepository, delegaciasRepository, criarRestTemplateApontandoParaWireMock());
	}

	private RestTemplate criarRestTemplateApontandoParaWireMock() {
		RestTemplate restTemplate = new RestTemplate();
		ClientHttpRequestFactory delegate = new SimpleClientHttpRequestFactory();
		int port = wm.getPort();

		restTemplate.setRequestFactory(new ClientHttpRequestFactory() {
			@Override
			public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
				URI local = UriComponentsBuilder.fromUri(uri)
						.scheme("http")
						.host("localhost")
						.port(port)
						.build(true)
						.toUri();
				return delegate.createRequest(local, httpMethod);
			}
		});

		return restTemplate;
	}

	private Delegacias delegacia() {
		return Delegacias.builder().id(1L).idSSP(ID_GRUPO).delegacia("1º DP").regiao("sul").build();
	}

	private void mockarRepositorios() {
		when(delegaciasRepository.findByIdSSP(ID_GRUPO)).thenReturn(Optional.of(delegacia()));
		when(naturezaRepository.findByNatureza(anyString())).thenReturn(Optional.empty());
		when(naturezaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
	}

	/**
	 * Planilha com 2 naturezas x 12 meses, valores distintos por natureza
	 * para permitir verificar o mapeamento coluna->mês linha a linha.
	 */
	private byte[] gerarPlanilhaFixture() throws IOException {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("dados");

			Row cabecalho = sheet.createRow(0);
			cabecalho.createCell(0).setCellValue("Natureza");
			for (int mes = 1; mes <= 12; mes++) {
				cabecalho.createCell(mes).setCellValue("Mes" + mes);
			}

			Row roubo = sheet.createRow(1);
			roubo.createCell(0).setCellValue("ROUBO");
			for (int mes = 1; mes <= 12; mes++) {
				roubo.createCell(mes).setCellValue(9 + mes); // 10..21
			}

			Row furto = sheet.createRow(2);
			furto.createCell(0).setCellValue("FURTO");
			for (int mes = 1; mes <= 12; mes++) {
				furto.createCell(mes).setCellValue(29 + mes); // 30..41
			}

			workbook.write(out);
			return out.toByteArray();
		}
	}

	@Test
	void buscarOcorrencias_deveEsperarConformeRetryAfterAntesDeRetentar() throws IOException {
		wm.stubFor(get(urlPathEqualTo(PATH))
				.inScenario("retry-after")
				.whenScenarioStateIs("Started")
				.willReturn(aResponse().withStatus(429).withHeader("Retry-After", "1"))
				.willSetStateTo("SEGUNDA_TENTATIVA"));

		wm.stubFor(get(urlPathEqualTo(PATH))
				.inScenario("retry-after")
				.whenScenarioStateIs("SEGUNDA_TENTATIVA")
				.willReturn(aResponse().withStatus(200).withHeader("Content-Type", XLSX_CONTENT_TYPE)
						.withBody(gerarPlanilhaFixture())));

		mockarRepositorios();

		long inicio = System.currentTimeMillis();
		sspClient.buscarOcorrencias(List.of(YearMonth.of(2024, 1)), ID_GRUPO);
		long duracao = System.currentTimeMillis() - inicio;

		// Retry-After: 1s. Se o código ignorasse o header e usasse o backoff
		// padrão (2s de base), a duração ficaria bem acima de 1.9s.
		assertTrue(duracao >= 900, "deveria aguardar ao menos ~1s conforme Retry-After, mas levou " + duracao + "ms");
		assertTrue(duracao < 1900, "não deveria cair no backoff padrão de 2s, mas levou " + duracao + "ms");

		wm.verify(2, getRequestedFor(urlPathEqualTo(PATH)));
	}

	@Test
	void buscarOcorrencias_deveAplicarBackoffPadraoQuandoSemRetryAfter() throws IOException {
		wm.stubFor(get(urlPathEqualTo(PATH))
				.inScenario("sem-retry-after")
				.whenScenarioStateIs("Started")
				.willReturn(aResponse().withStatus(429))
				.willSetStateTo("SEGUNDA_TENTATIVA"));

		wm.stubFor(get(urlPathEqualTo(PATH))
				.inScenario("sem-retry-after")
				.whenScenarioStateIs("SEGUNDA_TENTATIVA")
				.willReturn(aResponse().withStatus(200).withHeader("Content-Type", XLSX_CONTENT_TYPE)
						.withBody(gerarPlanilhaFixture())));

		mockarRepositorios();

		long inicio = System.currentTimeMillis();
		sspClient.buscarOcorrencias(List.of(YearMonth.of(2024, 1)), ID_GRUPO);
		long duracao = System.currentTimeMillis() - inicio;

		assertTrue(duracao >= 2000, "deveria aguardar ao menos o backoff padrão de 2s (tentativa 1), mas levou " + duracao + "ms");

		wm.verify(2, getRequestedFor(urlPathEqualTo(PATH)));
	}

	@Test
	@Timeout(60)
	void buscarOcorrencias_deveLancarExcecaoAposEsgotarTentativas() {
		// Sem Retry-After, todas as 5 tentativas batem 429. O código só expõe
		// o sleep via Thread.sleep privado (sem seam para acelerar em teste),
		// então este teste paga o custo real do backoff: 2+4+8+16 = 30s.
		wm.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(429)));

		RuntimeException excecao = assertThrows(RuntimeException.class,
				() -> sspClient.buscarOcorrencias(List.of(YearMonth.of(2024, 1)), ID_GRUPO));

		assertTrue(excecao.getMessage().contains("Limite de requisições da SSP excedido após 5 tentativas"));
		wm.verify(5, getRequestedFor(urlPathEqualTo(PATH)));
	}

	// ---------- corpo vazio ----------

	@Test
	void buscarOcorrencias_deveLancarErroQuandoCorpoVazio() {
		wm.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(204)));

		RuntimeException excecao = assertThrows(RuntimeException.class,
				() -> sspClient.buscarOcorrencias(List.of(YearMonth.of(2024, 1)), ID_GRUPO));

		assertTrue(excecao.getMessage().contains("Erro ao baixar Excel: resposta vazia"));
		wm.verify(1, getRequestedFor(urlPathEqualTo(PATH)));
	}

	@Test
	void buscarOcorrencias_deveParsearPlanilhaComDuasNaturezasEDozeMeses() throws IOException {
		wm.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
				.withHeader("Content-Type", XLSX_CONTENT_TYPE)
				.withBody(gerarPlanilhaFixture())));

		mockarRepositorios();

		List<YearMonth> anoTodo = IntStream.rangeClosed(1, 12)
				.mapToObj(mes -> YearMonth.of(2024, mes))
				.toList();

		List<Ocorrencia> resultado = sspClient.buscarOcorrencias(anoTodo, ID_GRUPO);

		assertEquals(24, resultado.size());
		assertEquals(12, resultado.stream().filter(o -> o.getNatureza().getNatureza().equals("ROUBO")).count());
		assertEquals(12, resultado.stream().filter(o -> o.getNatureza().getNatureza().equals("FURTO")).count());

		Ocorrencia roubojaneiro = resultado.stream()
				.filter(o -> o.getNatureza().getNatureza().equals("ROUBO") && o.getData().equals(LocalDate.of(2024, 1, 1)))
				.findFirst().orElseThrow();
		assertEquals(10, roubojaneiro.getQuantidade());

		Ocorrencia roubodezembro = resultado.stream()
				.filter(o -> o.getNatureza().getNatureza().equals("ROUBO") && o.getData().equals(LocalDate.of(2024, 12, 1)))
				.findFirst().orElseThrow();
		assertEquals(21, roubodezembro.getQuantidade());

		Ocorrencia furtojaneiro = resultado.stream()
				.filter(o -> o.getNatureza().getNatureza().equals("FURTO") && o.getData().equals(LocalDate.of(2024, 1, 1)))
				.findFirst().orElseThrow();
		assertEquals(30, furtojaneiro.getQuantidade());

		Ocorrencia furtodezembro = resultado.stream()
				.filter(o -> o.getNatureza().getNatureza().equals("FURTO") && o.getData().equals(LocalDate.of(2024, 12, 1)))
				.findFirst().orElseThrow();
		assertEquals(41, furtodezembro.getQuantidade());

		resultado.forEach(o -> assertEquals("1º DP", o.getDelegacia().getDelegacia()));
	}
}
