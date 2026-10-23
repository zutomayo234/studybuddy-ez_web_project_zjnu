package com.zjnu.studybuddy.service;

import com.zjnu.studybuddy.dto.ChatDTO;
import com.zjnu.studybuddy.vo.ChatVO;

public interface AiService {

    ChatVO chat(ChatDTO dto, Long userId);
}
