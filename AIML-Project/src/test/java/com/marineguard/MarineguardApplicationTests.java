package com.marineguard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MarineguardApplicationTests {  // ✓ CORRECT class name

    @Test
    void contextLoads() {
        System.out.println("Application context loaded successfully!");
    }

}