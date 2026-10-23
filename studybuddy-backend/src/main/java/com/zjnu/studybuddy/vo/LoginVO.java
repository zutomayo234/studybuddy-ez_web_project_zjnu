package com.zjnu.studybuddy.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LoginVO implements Serializable {

    private String token;

    private String tokenType = "Bearer";

    private Long expiresIn;

    private UserVO user;

    private List<String> roles;

    private List<String> permissions;
}
