package com.zjnu.studybuddy.controller;

import com.zjnu.studybuddy.common.PageResult;
import com.zjnu.studybuddy.common.Result;
import com.zjnu.studybuddy.dto.LogQueryDTO;
import com.zjnu.studybuddy.entity.OperationLog;
import com.zjnu.studybuddy.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "操作日志", description = "操作审计日志查询（功能增强）")
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @Operation(summary = "分页查询操作日志", description = "支持按模块、操作人、动作模糊匹配与成败筛选")
    @GetMapping
    @PreAuthorize("hasAuthority('log:list') or hasRole('ADMIN')")
    public Result<PageResult<OperationLog>> page(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer success,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LogQueryDTO query = new LogQueryDTO();
        query.setModule(module);
        query.setUsername(username);
        query.setAction(action);
        query.setSuccess(success);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return Result.success(logService.page(query));
    }
}
