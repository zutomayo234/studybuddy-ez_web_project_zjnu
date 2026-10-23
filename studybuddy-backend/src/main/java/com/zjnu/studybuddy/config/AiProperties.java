package com.zjnu.studybuddy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "studybuddy.ai")
public class AiProperties {

    private String baseUrl;

    private String apiKey;

    private String model;

    private int timeoutSeconds = 60;

    private String knowledgeFile = "classpath:kb/course-faq.txt";

    private int topK = 3;

    private int historyLimit = 8;
}
