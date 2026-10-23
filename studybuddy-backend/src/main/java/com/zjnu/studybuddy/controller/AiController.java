package com.zjnu.studybuddy.controller;

import com.zjnu.studybuddy.common.Result;
import com.zjnu.studybuddy.dto.ChatDTO;
import com.zjnu.studybuddy.security.LoginUser;
import com.zjnu.studybuddy.service.AiService;
import com.zjnu.studybuddy.service.ChatSessionService;
import com.zjnu.studybuddy.vo.ChatVO;
import com.zjnu.studybuddy.vo.MessageVO;
import com.zjnu.studybuddy.vo.SessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "智能问答", description = "学习搭子问答与多轮会话（实验五）")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final ChatSessionService chatSessionService;

    @Operation(summary = "智能问答", description = "输入校验 → 多轮上下文装配 → RAG 检索 → 调用大模型 → 落库")
    @PostMapping("/chat")
    @PreAuthorize("hasAuthority('ai:chat') or hasRole('STUDENT') or hasRole('ADMIN')")
    public Result<ChatVO> chat(@Valid @RequestBody ChatDTO dto,
                               @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(aiService.chat(dto, loginUser.getUser().getId()));
    }

    @Operation(summary = "查询会话列表", description = "返回当前用户的全部会话，按最近活跃时间倒序")
    @GetMapping("/sessions")
    @PreAuthorize("hasAuthority('ai:chat') or hasRole('STUDENT') or hasRole('ADMIN')")
    public Result<List<SessionVO>> sessions(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(chatSessionService.listSessions(loginUser.getUser().getId()));
    }

    @Operation(summary = "查询会话消息", description = "返回指定会话的全部历史消息（含出处与降级标记）")
    @GetMapping("/sessions/{id}/messages")
    @PreAuthorize("hasAuthority('ai:chat') or hasRole('STUDENT') or hasRole('ADMIN')")
    public Result<List<MessageVO>> messages(@PathVariable Long id,
                                            @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(chatSessionService.listMessages(loginUser.getUser().getId(), id));
    }

    @Operation(summary = "删除会话", description = "删除指定会话及其全部消息（仅限本人会话）")
    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("hasAuthority('ai:chat') or hasRole('STUDENT') or hasRole('ADMIN')")
    public Result<Void> deleteSession(@PathVariable Long id,
                                      @AuthenticationPrincipal LoginUser loginUser) {
        chatSessionService.deleteSession(loginUser.getUser().getId(), id);
        return Result.success("会话已删除", null);
    }
}
