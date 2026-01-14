package com.team2.reactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Reaction Service Application
 * 댓글 및 투표 기능을 담당하는 마이크로서비스
 *
 * @author Next-Page Team (정병진)
 */
@SpringBootApplication(scanBasePackages = {
    "com.team2.reactionservice",
    "com.team2.commonmodule"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
    "com.team2.reactionservice",
    "com.team2.commonmodule.feign"
})
@EnableJpaAuditing
public class ReactionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReactionServiceApplication.class, args);
    }
}
