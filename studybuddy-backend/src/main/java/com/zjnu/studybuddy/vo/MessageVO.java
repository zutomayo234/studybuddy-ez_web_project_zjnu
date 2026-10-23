package com.zjnu.studybuddy.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageVO implements Serializable {

    private Long id;

    private String role;

    private String content;

    private List<String> references;

    private Boolean fallback;

    private LocalDateTime createTime;
}
