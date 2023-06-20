package com.zerototen.savegame.config;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KakaoFeignConfig {
    @Bean
    public Client feignClient() {
        return new Client.Default(null, null);
    }
}