package com.zerototen.savegame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class SaveGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaveGameApplication.class, args);
    }

}