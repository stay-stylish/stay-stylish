package org.example.staystylish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StayStylishApplication {

    public static void main(String[] args) {
        SpringApplication.run(StayStylishApplication.class, args);
    }

}
