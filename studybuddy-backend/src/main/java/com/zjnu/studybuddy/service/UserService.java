package com.zjnu.studybuddy.service;

import com.zjnu.studybuddy.common.PageResult;
import com.zjnu.studybuddy.dto.UserCreateDTO;
import com.zjnu.studybuddy.dto.UserQueryDTO;
import com.zjnu.studybuddy.dto.UserUpdateDTO;
import com.zjnu.studybuddy.vo.UserVO;

public interface UserService {

    PageResult<UserVO> page(UserQueryDTO query);

    UserVO getById(Long id);

    UserVO create(UserCreateDTO dto);

    UserVO update(Long id, UserUpdateDTO dto);

    void delete(Long id);

    void resetPassword(Long id, String rawPassword);

    UserVO assignRoles(Long id, java.util.List<Long> roleIds);
}
