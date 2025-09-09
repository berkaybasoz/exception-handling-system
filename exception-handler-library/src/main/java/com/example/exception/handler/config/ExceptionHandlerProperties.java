package com.example.exception.handler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "exception.handler")
public class ExceptionHandlerProperties {
    private String projectName;
    private String podName;
    private String podIp;
    private String clusterName;
    
    private Kafka kafka = new Kafka();
    
    @Data
    public static class Kafka {
        private String topic = "exceptions";
        private String bootstrapServers = "localhost:9092";
    }
}