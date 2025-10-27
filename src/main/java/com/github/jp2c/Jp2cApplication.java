package com.github.jp2c;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Jp2cApplication {
    public static void main(String[] args) {
        SpringApplication.run(Jp2cApplication.class, args);
    }
}
