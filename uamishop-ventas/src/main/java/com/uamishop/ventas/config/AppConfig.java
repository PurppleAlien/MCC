package com.uamishop.ventas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // Timeout para establecer la conexión TCP (evita esperas infinitas si catálogo no responde)
        factory.setConnectTimeout(3_000);
        // Timeout para leer la respuesta (evita cuelgues si catálogo procesa lento)
        factory.setReadTimeout(8_000);
        return new RestTemplate(factory);
    }
}
