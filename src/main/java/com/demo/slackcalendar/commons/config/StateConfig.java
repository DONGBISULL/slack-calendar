package com.demo.slackcalendar.commons.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class StateConfig {
    /* 추후 상태 레디스로 변경 */
    @Bean
    public Map<String, Instant> slackStateStore() {
        return new ConcurrentHashMap<>();
    }

    /* 추후 상태 레디스로 변경 */
    @Bean
    public Map<String, Map<String, Object>> googleStateStore() {
        return new ConcurrentHashMap<>();
    }

}
