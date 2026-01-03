package com.topwatch.back_topwatch;

import com.topwatch.back_topwatch.repository.TokenRepository;
import com.topwatch.back_topwatch.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {BackTopwatchApplication.class})
class BackTopwatchApplicationTests {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenRepository tokenRepository;

    @Test
    void contextLoads() {
    }

}
