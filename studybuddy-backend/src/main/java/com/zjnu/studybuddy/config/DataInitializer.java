package com.zjnu.studybuddy.config;

import com.zjnu.studybuddy.entity.User;
import com.zjnu.studybuddy.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureUser("admin", "admin123", "系统管理员", "admin@zjnu.edu.cn", "13800000001", 1L, 1);
            ensureUser("teacher", "teacher123", "王老师", "teacher@zjnu.edu.cn", "13800000002", 2L, 1);
            ensureUser("student", "student123", "方野", "fangye@zjnu.edu.cn", "13800000003", 3L, 1);
        } catch (Exception e) {
            log.warn("默认账号初始化跳过（数据库可能未就绪）：{}", e.getMessage());
        }
    }

    private void ensureUser(String username, String rawPassword, String nickname,
                            String email, String phone, Long roleId, Integer gender) {
        if (userMapper.countByUsername(username) > 0) {
            return;
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setNickname(nickname);
        u.setEmail(email);
        u.setPhone(phone);
        u.setGender(gender);
        u.setStatus(1);
        u.setRemark("系统初始化账号");
        userMapper.insert(u);
        userMapper.insertUserRoles(u.getId(), List.of(roleId));
        log.info("已创建默认账号：{} / {}（角色ID={}）", username, rawPassword, roleId);
    }
}
