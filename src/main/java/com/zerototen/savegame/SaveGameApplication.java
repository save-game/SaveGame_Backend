package com.zerototen.savegame;

import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class SaveGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaveGameApplication.class, args);
    }

    @PostConstruct
    public void setTimeZone() {
        // timezone UTC 셋팅
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

}