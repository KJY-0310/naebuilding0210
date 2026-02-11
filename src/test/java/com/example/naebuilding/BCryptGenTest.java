package com.example.naebuilding;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptGenTest {
    @Test
    void gen() {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        System.out.println(enc.encode("admin1234"));
        System.out.println(enc.encode("user1234"));
    }
}
