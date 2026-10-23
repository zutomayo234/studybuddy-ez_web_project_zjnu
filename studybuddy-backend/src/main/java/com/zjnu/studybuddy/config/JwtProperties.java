package com.zjnu.studybuddy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "studybuddy.jwt")
public class JwtProperties {

    private String secret;

    private long expireMinutes = 120;

    private String header = "Authorization";

    private String prefix = "Bearer ";
}
