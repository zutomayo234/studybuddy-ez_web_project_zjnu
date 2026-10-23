package com.zjnu.studybuddy.service.impl;

import com.zjnu.studybuddy.common.BusinessException;
import com.zjnu.studybuddy.common.MaskUtil;
import com.zjnu.studybuddy.common.PageResult;
import com.zjnu.studybuddy.common.ResultCode;
import com.zjnu.studybuddy.dto.UserCreateDTO;
import com.zjnu.studybuddy.dto.UserQueryDTO;
import com.zjnu.studybuddy.dto.UserUpdateDTO;
import com.zjnu.studybuddy.entity.Role;
import com.zjnu.studybuddy.entity.User;
import com.zjnu.studybuddy.mapper.RoleMapper;
import com.zjnu.studybuddy.mapper.UserMapper;
import com.zjnu.studybuddy.service.UserService;
import com.zjnu.studybuddy.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Long ADMIN_ROLE_ID = 1L;

    private static final Long DEFAULT_ROLE_ID = 3L;

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> page(UserQueryDTO query) {
        long total = userMapper.countByQuery(query);
        List<User> rows = total == 0 ? List.of() : userMapper.selectPage(query);
        List<UserVO> list = rows.stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(query.getPageNum(), query.getPageSize(), total, list);
    }

    @Override
    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO create(UserCreateDTO dto) {

        if (userMapper.countByUsername(dto.getUsername()) > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在：" + dto.getUsername());
        }
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname(dto.getUsername());
        }

        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userMapper.insert(user);

        userMapper.insertUserRoles(user.getId(), List.of(DEFAULT_ROLE_ID));
        return getById(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO update(Long id, UserUpdateDTO dto) {
        User exist = userMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setId(id);
        userMapper.updateById(user);
        return getById(id);
    }

    @Override
    public void delete(Long id) {
        User exist = userMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        userMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long id, String rawPassword) {
        if (!StringUtils.hasText(rawPassword) || rawPassword.length() < 6) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "密码长度不能少于 6 位");
        }
        if (userMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        userMapper.updatePassword(id, passwordEncoder.encode(rawPassword));
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setPhone(MaskUtil.maskPhone(user.getPhone()));
        vo.setGenderText(switch (user.getGender() == null ? 0 : user.getGender()) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        });
        List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
        vo.setRoleCodes(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));
        vo.setRoleIds(roles.stream().map(Role::getId).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO assignRoles(Long id, List<Long> roleIds) {
        User exist = userMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        List<Long> targets = roleIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (targets.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "至少需要保留一个角色");
        }

        java.util.Set<Long> validRoleIds = roleMapper.selectAll().stream()
                .map(Role::getId).collect(Collectors.toSet());
        List<Long> illegal = targets.stream().filter(rid -> !validRoleIds.contains(rid)).toList();
        if (!illegal.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "存在非法的角色 ID：" + illegal);
        }

        boolean wasAdmin = roleMapper.selectRolesByUserId(id).stream()
                .anyMatch(r -> ADMIN_ROLE_ID.equals(r.getId()));
        boolean stillAdmin = targets.contains(ADMIN_ROLE_ID);
        if (wasAdmin && !stillAdmin && roleMapper.countUsersByRoleId(ADMIN_ROLE_ID) <= 1) {
            throw new BusinessException(ResultCode.CONFLICT,
                    "该用户是系统中最后一个管理员，不能移除其管理员角色");
        }
        userMapper.deleteUserRoles(id);
        userMapper.insertUserRoles(id, targets);
        return getById(id);
    }
}
