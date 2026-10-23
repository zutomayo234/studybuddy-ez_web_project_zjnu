package com.zjnu.studybuddy.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO implements Serializable {

    private Long id;
    private String username;
    private String nickname;
    private String email;

    private String phone;
    private String avatar;
    private String genderText;
    private Integer gender;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

    private List<String> roleCodes;

    private List<Long> roleIds;
}
