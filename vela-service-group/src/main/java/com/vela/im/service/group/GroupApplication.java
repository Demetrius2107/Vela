package com.vela.im.service.group;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.vela.im.service.group", "com.vela.im.service.common"})
public class GroupApplication {
    public static void main(String[] args) {
        SpringApplication.run(GroupApplication.class, args);
    }
}
