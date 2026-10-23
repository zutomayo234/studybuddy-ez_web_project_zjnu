package com.zjnu.studybuddy.controller;

import com.zjnu.studybuddy.annotation.OpLog;
import com.zjnu.studybuddy.common.Result;
import com.zjnu.studybuddy.dto.KbChunkDTO;
import com.zjnu.studybuddy.service.KbService;
import com.zjnu.studybuddy.vo.KbChunkVO;
import com.zjnu.studybuddy.vo.KbStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "知识库", description = "RAG 知识库查看与热更新（功能增强）")
@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbService kbService;

    @Operation(summary = "查询知识库片段", description = "返回全部片段快照，含序号、正文长度与是否运行时追加")
    @GetMapping("/chunks")
    @PreAuthorize("hasAuthority('kb:manage') or hasRole('ADMIN')")
    public Result<List<KbChunkVO>> chunks() {
        return Result.success(kbService.listChunks());
    }

    @Operation(summary = "知识库统计", description = "片段总数、运行时追加数、来源文件、Top-K 配置")
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('kb:manage') or hasRole('ADMIN')")
    public Result<KbStatsVO> stats() {
        return Result.success(kbService.stats());
    }

    @Operation(summary = "重新加载知识库", description = "重新解析知识库文件，保留运行时追加的片段")
    @PostMapping("/reload")
    @PreAuthorize("hasAuthority('kb:manage') or hasRole('ADMIN')")
    @OpLog(module = "知识库", action = "重新加载知识库")
    public Result<Map<String, Object>> reload() {
        int total = kbService.reload();
        return Result.success("知识库已重新加载",
                Map.of("chunkCount", total, "runtimeChunkCount", kbService.stats().getRuntimeChunkCount()));
    }

    @Operation(summary = "追加知识库片段", description = "向内存知识库追加一个片段，立即参与检索（进程重启后失效）")
    @PostMapping("/chunks")
    @PreAuthorize("hasAuthority('kb:manage') or hasRole('ADMIN')")
    @OpLog(module = "知识库", action = "追加知识库片段")
    public Result<Map<String, Object>> addChunk(@Valid @RequestBody KbChunkDTO dto) {
        int total = kbService.addChunk(dto.getContent());
        return Result.success("片段已追加，可立即参与检索",
                Map.of("chunkCount", total, "runtimeChunkCount", kbService.stats().getRuntimeChunkCount()));
    }
}
