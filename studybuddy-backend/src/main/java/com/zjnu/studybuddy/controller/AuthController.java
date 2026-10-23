package com.zjnu.studybuddy.controller;

import com.zjnu.studybuddy.annotation.OpLog;
import com.zjnu.studybuddy.common.Result;
import com.zjnu.studybuddy.dto.ChangePasswordDTO;
import com.zjnu.studybuddy.dto.LoginDTO;
import com.zjnu.studybuddy.dto.UserCreateDTO;
import com.zjnu.studybuddy.security.LoginUser;
import com.zjnu.studybuddy.service.AuthService;
import com.zjnu.studybuddy.vo.LoginVO;
import com.zjnu.studybuddy.vo.MeVO;
import com.zjnu.studybuddy.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证", description = "登录、注册、当前用户信息、修改本人密码")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.zjnu.studybuddy.service.UserService userService;

    @Operation(summary = "用户登录", description = "校验用户名密码并签发 JWT，同时下发角色与权限编码")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success("登录成功", authService.login(dto));
    }

    @Operation(summary = "用户注册", description = "注册后默认分配学生角色")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success("注册成功", authService.register(dto));
    }

    @Operation(summary = "查询当前用户", description = "返回当前登录用户的资料、角色与细粒度权限")
    @GetMapping("/me")
    public Result<MeVO> me(@AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(authService.me(loginUser.getUser().getId()));
    }

    @Operation(summary = "修改本人密码", description = "必须提供正确的旧密码；新密码经 BCrypt 重新加密后入库")
    @PutMapping("/password")
    @OpLog(module = "认证", action = "修改本人密码")
    public Result<Void> changePassword(@AuthenticationPrincipal LoginUser loginUser,
                                       @Valid @RequestBody ChangePasswordDTO dto) {
        if (loginUser == null) {
            return Result.error(401, "未登录");
        }
        authService.changePassword(loginUser.getUser().getId(), dto);
        return Result.success("密码修改成功，请使用新密码重新登录", null);
    }
}
