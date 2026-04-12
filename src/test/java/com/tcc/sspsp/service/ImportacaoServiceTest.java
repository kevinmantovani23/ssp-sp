package com.tcc.sspsp.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ImportacaoServiceTest {

    @Autowired
    private ImportacaoService importacaoService;

    @Test
    void deveImportarDadosSSP() {
        System.out.println("Iniciando importação...");
        
        importacaoService.importarDadosSSP();
        
        System.out.println("Finalizado!");
    }
}