package com.zjnu.studybuddy.service.impl;

import com.zjnu.studybuddy.ai.RagService;
import com.zjnu.studybuddy.service.KbService;
import com.zjnu.studybuddy.vo.KbChunkVO;
import com.zjnu.studybuddy.vo.KbStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KbServiceImpl implements KbService {

    private final RagService ragService;

    @Override
    public List<KbChunkVO> listChunks() {
        return ragService.snapshot();
    }

    @Override
    public KbStatsVO stats() {
        KbStatsVO vo = new KbStatsVO();
        vo.setChunkCount(ragService.chunkCount());
        vo.setRuntimeChunkCount(ragService.runtimeChunkCount());
        vo.setSourceFile(ragService.sourceFile());
        vo.setTopK(ragService.topK());
        return vo;
    }

    @Override
    public int reload() {
        return ragService.reload();
    }

    @Override
    public int addChunk(String content) {
        return ragService.addChunk(content);
    }
}
