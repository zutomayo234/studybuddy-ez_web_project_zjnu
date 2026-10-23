package com.zjnu.studybuddy.security;

import com.zjnu.studybuddy.entity.Role;
import com.zjnu.studybuddy.entity.User;
import com.zjnu.studybuddy.mapper.PermissionMapper;
import com.zjnu.studybuddy.mapper.RoleMapper;
import com.zjnu.studybuddy.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        List<String> roleCodes = roleMapper.selectRolesByUserId(user.getId())
                .stream().map(Role::getRoleCode).toList();
        var permissions = new HashSet<>(permissionMapper.selectPermCodesByUserId(user.getId()));
        return new LoginUser(user, roleCodes, permissions);
    }
}
