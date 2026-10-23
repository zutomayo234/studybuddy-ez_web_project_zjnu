package com.zjnu.studybuddy.ai;

import com.zjnu.studybuddy.config.AiProperties;
import com.zjnu.studybuddy.vo.KbChunkVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagService {

    private final AiProperties props;
    private final ResourceLoader resourceLoader;

    private final List<Chunk> chunks = new CopyOnWriteArrayList<>();

    private final List<String> runtimeTexts = new CopyOnWriteArrayList<>();

    private static final Set<String> STOP_WORDS = Set.of(
            "怎么", "什么", "哪些", "哪个", "如何", "是否", "可以", "能不能", "能否",
            "是不是", "有没有", "为什么", "因为", "所以", "但是", "然后", "如果", "这个", "那个",
            "我们", "你们", "他们", "自己", "一个", "一些", "一下", "这样", "那样", "时候",
            "需要", "应该", "可能", "已经", "还是", "或者", "以及", "并且", "而且", "就是", "不是",
            "请问", "告诉", "帮我", "给我", "想要", "希望", "进行", "对于", "关于", "通过",
            "the", "and", "for", "are", "was", "what", "how", "why", "can", "you", "your",
            "please", "tell", "about", "with", "this", "that", "is", "of", "to", "in"
    );

    public RagService(AiProperties props, ResourceLoader resourceLoader) {
        this.props = props;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        int total = reload();
        log.info("RAG 知识库加载完成，共 {} 个片段", total);
    }

    public synchronized int reload() {
        List<Chunk> loaded = new ArrayList<>();
        int idx = 0;
        try {
            Resource resource = resourceLoader.getResource(props.getKnowledgeFile());
            if (!resource.exists()) {
                log.warn("知识库文件不存在: {}", props.getKnowledgeFile());
            } else {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    String text = reader.lines().collect(Collectors.joining("\n"));
                    for (String block : splitIntoBlocks(text)) {
                        loaded.add(new Chunk(++idx, block, tokenize(block), false));
                    }
                }
                log.info("知识库文件解析完成，文件片段 {} 个", loaded.size());
            }
        } catch (Exception e) {
            log.error("加载知识库失败", e);
        }

        for (String runtimeText : runtimeTexts) {
            loaded.add(new Chunk(++idx, runtimeText, tokenize(runtimeText), true));
        }
        chunks.clear();
        chunks.addAll(loaded);
        return chunks.size();
    }

    public synchronized int addChunk(String text) {
        String t = text == null ? "" : text.strip();
        if (t.isEmpty()) {
            return chunks.size();
        }
        runtimeTexts.add(t);
        chunks.add(new Chunk(chunks.size() + 1, t, tokenize(t), true));
        return chunks.size();
    }

    public List<KbChunkVO> snapshot() {
        return chunks.stream().map(c -> {
            KbChunkVO vo = new KbChunkVO();
            vo.setIndex(c.index);
            vo.setText(c.text);
            vo.setTokenCount(c.tokens.size());
            vo.setRuntimeAdded(c.runtime);
            return vo;
        }).toList();
    }

    public int chunkCount() {
        return chunks.size();
    }

    public int runtimeChunkCount() {
        return runtimeTexts.size();
    }

    public String sourceFile() {
        return props.getKnowledgeFile();
    }

    public int topK() {
        return props.getTopK();
    }

    private List<String> splitIntoBlocks(String text) {
        List<String> blocks = new ArrayList<>();
        for (String block : text.split("\\n\\s*\\n")) {
            String t = block.strip();
            if (t.isEmpty() || t.startsWith("#")) {
                continue;
            }
            blocks.add(t);
        }
        return blocks;
    }

    public List<String> retrieve(String question) {
        if (chunks.isEmpty() || !StringUtils.hasText(question)) {
            return List.of();
        }
        Set<String> qTokens = tokenize(question);

        List<String> meaningful = qTokens.stream()
                .filter(this::isMeaningful)
                .toList();

        int requiredHits = Math.min(2, Math.max(meaningful.size(), 1));

        return chunks.stream()
                .map(c -> new Scored(c, score(qTokens, meaningful, c.tokens, requiredHits)))
                .filter(s -> s.score > 0)
                .sorted(Comparator.comparingDouble((Scored s) -> s.score).reversed())
                .limit(props.getTopK())
                .map(s -> "第" + s.chunk.index + "条：" + s.chunk.text)
                .collect(Collectors.toList());
    }

    private double score(Set<String> queryTokens, List<String> meaningful,
                         Set<String> docTokens, int requiredHits) {
        long meaningfulHit = meaningful.stream().filter(docTokens::contains).count();
        if (meaningfulHit < requiredHits) {
            return 0;
        }

        long singleHit = queryTokens.stream()
                .filter(t -> t.length() == 1)
                .filter(docTokens::contains)
                .count();
        return (meaningfulHit * 2.0 + singleHit * 0.3) / Math.sqrt(Math.max(docTokens.size(), 1));
    }

    private boolean isMeaningful(String token) {
        return token.length() >= 2 && !STOP_WORDS.contains(token);
    }

    private Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        String lower = text.toLowerCase();
        StringBuilder chinese = new StringBuilder();
        StringBuilder word = new StringBuilder();
        for (char c : lower.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fff') {
                chinese.append(c);
                if (word.length() > 0) {
                    tokens.add(word.toString());
                    word.setLength(0);
                }
            } else if (Character.isLetterOrDigit(c)) {
                word.append(c);
                if (chinese.length() > 0) {
                    addChinese(tokens, chinese.toString());
                    chinese.setLength(0);
                }
            } else {
                if (word.length() > 0) {
                    tokens.add(word.toString());
                    word.setLength(0);
                }
                if (chinese.length() > 0) {
                    addChinese(tokens, chinese.toString());
                    chinese.setLength(0);
                }
            }
        }
        if (word.length() > 0) {
            tokens.add(word.toString());
        }
        if (chinese.length() > 0) {
            addChinese(tokens, chinese.toString());
        }
        return tokens;
    }

    private void addChinese(Set<String> tokens, String segment) {
        for (int i = 0; i < segment.length(); i++) {
            tokens.add(String.valueOf(segment.charAt(i)));
            if (i + 1 < segment.length()) {
                tokens.add(segment.substring(i, i + 2));
            }
        }
    }

    private record Chunk(int index, String text, Set<String> tokens, boolean runtime) {
    }

    private record Scored(Chunk chunk, double score) {
    }
}
