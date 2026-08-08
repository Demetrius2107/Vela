package com.vela.im.service.conversation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.vela.im.service.conversation", "com.vela.im.service.common", "com.vela.im.shared"})
@MapperScan("com.vela.im.service.conversation.infrastructure.persistence.mapper")
public class ConversationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConversationApplication.class, args);
    }
}
