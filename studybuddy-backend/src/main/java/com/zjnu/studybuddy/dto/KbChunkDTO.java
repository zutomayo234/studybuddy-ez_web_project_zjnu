package com.zjnu.studybuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KbChunkDTO {

    @NotBlank(message = "片段内容不能为空")
    @Size(max = 1000, message = "单个片段不超过 1000 字")
    private String content;
}
