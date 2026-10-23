package com.zjnu.studybuddy;

import com.zjnu.studybuddy.ai.RagService;
import com.zjnu.studybuddy.config.AiProperties;
import com.zjnu.studybuddy.vo.KbChunkVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RagChunkManageTest {

    private RagService ragService;

    @BeforeEach
    void setUp() {
        AiProperties props = new AiProperties();
        props.setKnowledgeFile("classpath:kb/course-faq.txt");
        props.setTopK(5);
        ragService = new RagService(props, new DefaultResourceLoader());
        ragService.init();
    }

    @Test
    @DisplayName("初始加载后应有片段，且不存在运行时追加片段")
    void shouldLoadFileChunksOnInit() {
        assertTrue(ragService.chunkCount() > 0, "知识库文件应解析出片段");
        assertEquals(0, ragService.runtimeChunkCount());
        assertFalse(ragService.sourceFile().isEmpty());
    }

    @Test
    @DisplayName("追加片段后片段数 +1，且能被检索命中")
    void addedChunkShouldBeRetrievable() {
        int before = ragService.chunkCount();
        String text = "微服务熔断降级：当下游服务持续失败时，调用方应快速失败而不是无限重试，"
                + "常用实现为熔断器三态（关闭/打开/半开）配合降级兜底逻辑。";

        int after = ragService.addChunk(text);

        assertEquals(before + 1, after);
        assertEquals(1, ragService.runtimeChunkCount());

        List<String> refs = ragService.retrieve("微服务熔断降级怎么做？");
        assertTrue(refs.stream().anyMatch(r -> r.contains("熔断")),
                "新追加的片段应能参与检索并命中");

        List<KbChunkVO> snapshot = ragService.snapshot();
        assertTrue(snapshot.stream().anyMatch(c -> Boolean.TRUE.equals(c.getRuntimeAdded()) && text.equals(c.getText())),
                "快照中应标记该片段为运行时追加");
    }

    @Test
    @DisplayName("重新加载文件后，运行时追加的片段仍然保留")
    void reloadShouldKeepRuntimeChunks() {
        String text = "事务隔离级别：读未提交、读已提交、可重复读、串行化，隔离性越强并发度越低。";
        ragService.addChunk(text);
        int beforeReload = ragService.chunkCount();

        int afterReload = ragService.reload();

        assertEquals(beforeReload, afterReload, "重载后片段总数应保持不变");
        assertEquals(1, ragService.runtimeChunkCount());
        assertTrue(ragService.snapshot().stream().anyMatch(c -> text.equals(c.getText())));
    }

    @Test
    @DisplayName("空白片段不入库，片段数不变")
    void blankChunkShouldBeIgnored() {
        int before = ragService.chunkCount();
        assertEquals(before, ragService.addChunk("   "));
        assertEquals(before, ragService.addChunk(null));
        assertEquals(0, ragService.runtimeChunkCount());
    }
}
