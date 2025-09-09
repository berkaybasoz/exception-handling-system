package com.example.exception.handler.config;

import com.example.exception.handler.service.ExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@EnableConfigurationProperties(ExceptionHandlerProperties.class)
@Import(KafkaConfig.class)
public class ExceptionHandlerAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ExceptionHandler exceptionHandler(KafkaTemplate<String, String> kafkaTemplate,
                                           ExceptionHandlerProperties properties,
                                           ObjectMapper objectMapper) {
        return new ExceptionHandler(kafkaTemplate, properties, objectMapper);
    }
}