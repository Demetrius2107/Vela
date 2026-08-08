package com.vela.im.service.friendship;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.vela.im.service.friendship", "com.vela.im.service.common", "com.vela.im.shared"})
@EnableFeignClients
public class FriendshipApplication {
    public static void main(String[] args) {
        SpringApplication.run(FriendshipApplication.class, args);
    }
}
