package com.topwatch.back_topwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackTopwatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackTopwatchApplication.class, args);
    }

}
