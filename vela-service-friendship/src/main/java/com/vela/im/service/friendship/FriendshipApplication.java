package com.vela.im.service.friendship;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.vela.im.service.friendship", "com.vela.im.service.common"})
public class FriendshipApplication {
    public static void main(String[] args) {
        SpringApplication.run(FriendshipApplication.class, args);
    }
}
