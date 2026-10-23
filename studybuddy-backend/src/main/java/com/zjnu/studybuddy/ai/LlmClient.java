package com.zjnu.studybuddy.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjnu.studybuddy.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LlmClient {

    private final AiProperties props;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public LlmClient(AiProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(props.getTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    public boolean isConfigured() {
        return props.getApiKey() != null && !props.getApiKey().isBlank();
    }

    public String getModel() {
        return props.getModel();
    }

    public String chat(String systemPrompt, String userPrompt) {
        return chat(List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
    }

    public String chat(List<Map<String, String>> messages) {
        Map<String, Object> payload = Map.of(
                "model", props.getModel(),
                "messages", messages,
                "temperature", 0.3,
                "stream", false
        );

        String responseBody = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + props.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                log.warn("模型返回结构异常: {}", responseBody);
                return "模型未返回有效内容，请稍后重试。";
            }
            return content.asText();
        } catch (Exception e) {
            log.error("解析模型响应失败", e);
            return "模型返回解析失败：" + e.getMessage();
        }
    }
}
