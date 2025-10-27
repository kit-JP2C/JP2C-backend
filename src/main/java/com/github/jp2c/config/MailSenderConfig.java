package com.github.jp2c.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailSenderConfig {
    @Bean
    @Profile("!k8s")
    public JavaMailSender getMockJavaMailSender() {
        return new JavaMailSenderImpl();
    }
}
