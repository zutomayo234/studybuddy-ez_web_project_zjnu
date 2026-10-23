package com.zjnu.studybuddy.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MeVO implements Serializable {

    private UserVO user;

    private List<String> roles;

    private List<String> permissions;
}
