package com.zjnu.studybuddy.service;

import com.zjnu.studybuddy.dto.ChangePasswordDTO;
import com.zjnu.studybuddy.dto.LoginDTO;
import com.zjnu.studybuddy.vo.LoginVO;
import com.zjnu.studybuddy.vo.MeVO;
import com.zjnu.studybuddy.vo.UserVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    UserVO register(com.zjnu.studybuddy.dto.UserCreateDTO dto);

    MeVO me(Long userId);

    void changePassword(Long userId, ChangePasswordDTO dto);
}
