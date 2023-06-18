package com.zerototen.savegame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SaveGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaveGameApplication.class, args);
    }

}