package com.ebkb.everywhere.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // AI 서버 주소 (FastAPI) 설정
    // application.properties 파일에 ai.server.url=http://localhost:8000 와 같이 설정해야 합니다.
    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getAiServerUrl() {
        return aiServerUrl;
    }
}