package com.zjnu.studybuddy;

import com.zjnu.studybuddy.ai.RagService;
import com.zjnu.studybuddy.config.AiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RagServiceTest {

    private RagService ragService;

    @BeforeEach
    void setUp() {
        AiProperties props = new AiProperties();
        props.setKnowledgeFile("classpath:kb/course-faq.txt");
        props.setTopK(3);
        ragService = new RagService(props, new DefaultResourceLoader());
        ragService.init();
    }

    @Test
    @DisplayName("提问涉及 BCrypt 时应能检索到密码加密相关片段")
    void shouldRetrievePasswordChunk() {
        List<String> refs = ragService.retrieve("密码能不能明文存库？BCrypt 怎么校验？");
        assertFalse(refs.isEmpty(), "应至少召回一条片段");
        assertTrue(refs.stream().anyMatch(r -> r.contains("BCrypt")), "应召回含 BCrypt 的片段");
    }

    @Test
    @DisplayName("提问涉及跨域时应能检索到 CORS 相关片段")
    void shouldRetrieveCorsChunk() {
        List<String> refs = ragService.retrieve("前后端分离端口不一样，跨域怎么解决？");
        assertFalse(refs.isEmpty());
        assertTrue(refs.stream().anyMatch(r -> r.contains("跨域") || r.contains("CORS")));
    }

    @Test
    @DisplayName("完全不相关的提问不应召回（分数为 0 被过滤）")
    void unrelatedQuestionShouldReturnEmpty() {
        List<String> refs = ragService.retrieve("今天天气怎么样适合钓鱼吗");
        assertTrue(refs.isEmpty(), "无关问题应无召回，避免放大幻觉");
    }

    @Test
    @DisplayName("只有一个关键词的短提问仍应召回（阈值放宽为 1）")
    void singleKeywordQuestionShouldStillRetrieve() {
        List<String> refs = ragService.retrieve("MyBatis 怎么配置？");
        assertFalse(refs.isEmpty(), "单一实义词提问应放宽阈值，保证召回");
        assertTrue(refs.stream().anyMatch(r -> r.contains("MyBatis")));
    }
}
