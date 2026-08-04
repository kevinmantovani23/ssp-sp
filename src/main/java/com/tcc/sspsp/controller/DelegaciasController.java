package com.tcc.sspsp.controller;

import com.tcc.sspsp.dto.ApiResponseDTO;
import com.tcc.sspsp.dto.DelegaciasDTO;
import com.tcc.sspsp.service.DelegaciasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/delegacias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")   // em prod, restringir para o domínio do frontend
@Tag(name = "Delegacias", description = "Endpoints de delegacias da SSP-SP")
public class DelegaciasController {

    private final DelegaciasService service;

    // ── GET /v1/delegacias ─────────────────────────────────────────────────
    @GetMapping
    @Operation(
        summary = "Lista delegacias com filtros opcionais",
        description = "Retorna delegacias filtradas por região, nome e idSSP. Quando mais de um filtro é informado, eles são combinados. Sem filtros, retorna todas as delegacias."
    )
    public ResponseEntity<ApiResponseDTO<List<DelegaciasDTO>>> listar(
        @Parameter(description = "Região da delegacia (ex: Zona Norte)")
        @RequestParam(required = false) String regiao,

        @Parameter(description = "Nome (ou parte do nome) da delegacia")
        @RequestParam(required = false) String delegacia

    ) {
        return ResponseEntity.ok(ApiResponseDTO.ok(service.listarComFiltros(regiao, delegacia)));
    }

    // ── GET /v1/delegacias/{id} ─────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(
        summary = "Busca uma delegacia por ID",
        description = "Retorna os dados de uma única delegacia."
    )
    public ResponseEntity<ApiResponseDTO<DelegaciasDTO>> buscarPorId(
        @Parameter(description = "ID da delegacia", required = true)
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponseDTO.ok(service.buscarPorId(id)));
    }
}
