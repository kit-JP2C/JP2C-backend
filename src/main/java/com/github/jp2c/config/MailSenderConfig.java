package com.github.jp2c.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@Slf4j
public class MailSenderConfig {

    @Bean
    @Profile("k8s")
    public JavaMailSender realMailSender(JavaMailSender sender) {
        return sender;
    }

    @Bean
    @Profile("!k8s")
    public JavaMailSender mockMailSender() {
        return new JavaMailSenderImpl() {
            @Override
            public void send(SimpleMailMessage... simpleMessages) {
                log.info("[Mock] 이메일 전송 스킵됨");
            }
        };
    }
}
