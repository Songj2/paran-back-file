package com.paranmazang.paran;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ParanApplicationTests {

    @Autowired
    EntityManager em;

    @Test
    void contextLoads() {
    }
}
