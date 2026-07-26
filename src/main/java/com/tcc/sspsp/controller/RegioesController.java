package com.tcc.sspsp.controller;


import com.tcc.sspsp.dto.ApiResponseDTO;
import com.tcc.sspsp.service.RegioesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/regioes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Regioes", description = "Tipos de ocorrência disponíveis")
public class RegioesController {

    private final RegioesService service;

    @GetMapping
    @Operation(
            summary = "Lista as regiões disponíveis",
            description = "Retorna as regiões disponíveis para listagem."
    )
    public ResponseEntity<ApiResponseDTO<List<String>>> listar(){
        return ResponseEntity.ok(ApiResponseDTO.ok(service.listarRegioes()));
    }

}
