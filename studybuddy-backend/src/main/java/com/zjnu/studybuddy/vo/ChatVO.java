package com.zjnu.studybuddy.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ChatVO implements Serializable {

    private String answer;

    private List<String> references;

    private String model;

    private Boolean fallback = false;

    private Long sessionId;

    private Integer contextSize = 0;
}
