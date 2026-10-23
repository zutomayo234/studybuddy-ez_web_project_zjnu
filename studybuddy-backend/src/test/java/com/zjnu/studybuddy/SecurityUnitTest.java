package com.zjnu.studybuddy;

import com.zjnu.studybuddy.common.MaskUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityUnitTest {

    @Test
    @DisplayName("BCrypt：同一明文两次加密结果不同，但都能校验通过")
    void bcryptShouldUseRandomSalt() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "123456";
        String c1 = encoder.encode(raw);
        String c2 = encoder.encode(raw);

        assertNotEquals(c1, c2, "随机盐应导致两次密文不同");
        assertTrue(encoder.matches(raw, c1));
        assertTrue(encoder.matches(raw, c2));
        assertFalse(encoder.matches("wrong", c1));
    }

    @Test
    @DisplayName("手机号与邮箱脱敏")
    void maskShouldWork() {
        assertEquals("138****5678", MaskUtil.maskPhone("13812345678"));
        assertEquals("z****n@qq.com", MaskUtil.maskEmail("zhangsan@qq.com"));
    }
}
