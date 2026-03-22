package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de E-commerce")
                        .version("1.0")
                        .description("API REST para gestión de catálogo, carrito y órdenes")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("equipo@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("Licencia de uso")
                                .url("https://example.com/licencia")));
    }
}