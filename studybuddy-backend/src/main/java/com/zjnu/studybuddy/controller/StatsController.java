package com.zjnu.studybuddy.controller;

import com.zjnu.studybuddy.common.Result;
import com.zjnu.studybuddy.service.StatsService;
import com.zjnu.studybuddy.vo.StatsOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据看板", description = "首页关键指标统计（功能增强）")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "查询概览指标", description = "用户、角色、权限、会话、消息、知识库、审计日志等聚合统计")
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('stats:view') or hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public Result<StatsOverviewVO> overview() {
        return Result.success(statsService.overview());
    }
}
