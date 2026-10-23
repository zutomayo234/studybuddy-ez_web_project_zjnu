package com.zjnu.studybuddy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class User implements Serializable {

    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Integer gender;

    private Integer status;

    private String remark;

    @JsonIgnore
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<Role> roles;
}
