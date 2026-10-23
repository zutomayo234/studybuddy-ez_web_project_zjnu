package com.zjnu.studybuddy.service.impl;

import com.zjnu.studybuddy.common.BusinessException;
import com.zjnu.studybuddy.common.ResultCode;
import com.zjnu.studybuddy.dto.ChangePasswordDTO;
import com.zjnu.studybuddy.dto.LoginDTO;
import com.zjnu.studybuddy.entity.Role;
import com.zjnu.studybuddy.entity.User;
import com.zjnu.studybuddy.mapper.PermissionMapper;
import com.zjnu.studybuddy.mapper.RoleMapper;
import com.zjnu.studybuddy.mapper.UserMapper;
import com.zjnu.studybuddy.security.JwtUtil;
import com.zjnu.studybuddy.service.AuthService;
import com.zjnu.studybuddy.service.UserService;
import com.zjnu.studybuddy.vo.LoginVO;
import com.zjnu.studybuddy.vo.MeVO;
import com.zjnu.studybuddy.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectByUsername(dto.getUsername());

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }

        List<String> roleCodes = roleMapper.selectRolesByUserId(user.getId())
                .stream().map(Role::getRoleCode).toList();
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCodes);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresIn(jwtUtil.getExpiresInSeconds());
        vo.setUser(userService.getById(user.getId()));
        vo.setRoles(roleCodes);

        vo.setPermissions(permissionMapper.selectPermCodesByUserId(user.getId()));
        log.info("用户 {} 登录成功，角色 {}", user.getUsername(), roleCodes);
        return vo;
    }

    @Override
    public UserVO register(com.zjnu.studybuddy.dto.UserCreateDTO dto) {
        return userService.create(dto);
    }

    @Override
    public MeVO me(Long userId) {
        MeVO vo = new MeVO();
        vo.setUser(userService.getById(userId));
        vo.setRoles(roleMapper.selectRolesByUserId(userId)
                .stream().map(Role::getRoleCode).toList());
        vo.setPermissions(permissionMapper.selectPermCodesByUserId(userId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "旧密码不正确");
        }
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新密码不能与旧密码相同");
        }
        if (!StringUtils.hasText(dto.getNewPassword()) || dto.getNewPassword().length() < 6) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新密码长度不能少于 6 位");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(dto.getNewPassword()));
        log.info("用户 {} 修改了本人密码", user.getUsername());
    }
}
