package com.team2.storyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Story Service Application
 * 소설 콘텐츠 및 실시간 기능을 담당하는 마이크로서비스
 *
 * @author Next-Page Team (정진호)
 */
@SpringBootApplication(scanBasePackages = {
    "com.team2.storyservice",
    "com.team2.commonmodule"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
    "com.team2.storyservice",
    "com.team2.commonmodule.feign"
})
@EnableJpaAuditing
public class StoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StoryServiceApplication.class, args);
    }
}
