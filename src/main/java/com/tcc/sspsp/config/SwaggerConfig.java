package com.tcc.sspsp.config;
 
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
@Configuration
public class SwaggerConfig {
 
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SSP-SP — API de Segurança Pública")
                .description("""
                    API pública desenvolvida como TCC para democratizar o acesso
                    aos dados de segurança pública do Estado de São Paulo.
                    
                    Dados originados da Secretaria de Segurança Pública de SP (SSP-SP).
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Time TCC")
                    .email("tcc@exemplo.com"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT"))
            );
    }
}