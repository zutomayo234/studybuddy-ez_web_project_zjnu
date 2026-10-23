package com.zjnu.studybuddy.controller;

import com.zjnu.studybuddy.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "健康检查", description = "连通性测试与健康探针（实验一）")
@RestController
@RequestMapping("/api/hello")
public class HelloController {

    @Value("${spring.application.name:studybuddy}")
    private String appName;

    @Operation(summary = "连通性测试", description = "返回应用名、问候语与服务器时间，用于验证服务已启动")
    @GetMapping
    public Result<Map<String, Object>> hello() {
        Map<String, Object> data = new HashMap<>();
        data.put("app", appName);
        data.put("message", "Hello, 学习搭子 StudyBuddy!");
        data.put("time", LocalDateTime.now().toString());
        return Result.success(data);
    }
}
