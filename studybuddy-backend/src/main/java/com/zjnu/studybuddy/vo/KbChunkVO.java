package com.zjnu.studybuddy.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class KbChunkVO implements Serializable {

    private Integer index;

    private String text;

    private Integer tokenCount;

    private Boolean runtimeAdded;
}
