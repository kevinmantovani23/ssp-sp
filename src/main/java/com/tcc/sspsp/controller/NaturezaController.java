package com.tcc.sspsp.controller;

import com.tcc.sspsp.dto.ApiResponseDTO;
import com.tcc.sspsp.model.Natureza;
import com.tcc.sspsp.repository.NaturezaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/naturezas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Naturezas", description = "Tipos de ocorrência disponíveis")
public class NaturezaController {

    private final NaturezaRepository repository;

    @GetMapping
    @Operation(summary = "Lista todas as naturezas de ocorrência")
    public ResponseEntity<ApiResponseDTO<List<Natureza>>> listar() {
        return ResponseEntity.ok(ApiResponseDTO.ok(repository.findAll()));
    }
}