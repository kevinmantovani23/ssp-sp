package com.tcc.sspsp.controller;

import com.tcc.sspsp.dto.ApiResponseDTO;
import com.tcc.sspsp.service.ImportacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Operações administrativas de carga de dados.
 * Só é registrado quando app.importacao.endpoint-habilitado = true — o endpoint
 * não possui autenticação e dispara centenas de requisições à SSP-SP, portanto
 * deve permanecer desabilitado em produção.
 */
@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.importacao.endpoint-habilitado", havingValue = "true")
@Tag(name = "Administração", description = "Carga de dados da SSP-SP. Desabilitar em produção, serve como contingência ou primeira execução.")
public class ImportacaoController {

    private final ImportacaoService service;

    // ── POST /v1/admin/importacao ──────────────────────────────────────────
    @PostMapping("/importacao")
    @Operation(
        summary = "Dispara a importação dos dados da SSP-SP em segundo plano",
        description = "Responde 202 imediatamente e executa a carga em thread dedicada, pois o processo demora para finalizar. "
                + "O progresso é registrado no log da aplicação. Responde 409 caso já exista uma importação em andamento."
    )
    public ResponseEntity<ApiResponseDTO<String>> importar() {
        if (!service.dispararEmSegundoPlano()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDTO.error("IMPORTACAO_EM_ANDAMENTO", "Já existe uma importação em andamento."));
        }

        return ResponseEntity.accepted()
            .body(ApiResponseDTO.ok("Importação iniciada em segundo plano. Acompanhe o progresso no log da aplicação."));
    }

    // ── GET /v1/admin/importacao/status ────────────────────────────────────
    @GetMapping("/importacao/status")
    @Operation(
        summary = "Informa se há importação em andamento",
        description = "Retorna true enquanto uma importação estiver executando."
    )
    public ResponseEntity<ApiResponseDTO<Boolean>> status() {
        return ResponseEntity.ok(ApiResponseDTO.ok(service.isEmExecucao()));
    }
}
