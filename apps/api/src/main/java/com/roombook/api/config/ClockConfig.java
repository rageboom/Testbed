package com.roombook.api.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** '오늘' · '지난 시각' · '시작 30분 전' 을 판정하는 시계. 서버 시각, Asia/Seoul 고정이다(ADR-RESERVATION-002). */
@Configuration
public class ClockConfig {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock clock() {
        return Clock.system(ZONE);
    }
}
