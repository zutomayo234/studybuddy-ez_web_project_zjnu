package com.zjnu.studybuddy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "studybuddy.cors")
public class CorsProperties {

    private List<String> allowedOrigins;
}
