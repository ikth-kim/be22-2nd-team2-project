package com.team2.gatewayserver.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API 문서 집계를 위한 라우팅 설정
 * YAML 설정의 리스트 덮어쓰기 문제를 방지하기 위해 Java Config로 설정
 */
@Configuration
public class SwaggerRouteConfig {

    @Bean
    public RouteLocator swaggerRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("member-service-docs", r -> r
                        .path("/v3/api-docs/member/**", "/v3/api-docs/member")
                        .filters(f -> f.rewritePath("/v3/api-docs/member(?<segment>.*)", "/v3/api-docs${segment}"))
                        .uri("lb://MEMBER-SERVICE"))
                .route("story-service-docs", r -> r
                        .path("/v3/api-docs/story/**", "/v3/api-docs/story")
                        .filters(f -> f.rewritePath("/v3/api-docs/story(?<segment>.*)", "/v3/api-docs${segment}"))
                        .uri("lb://STORY-SERVICE"))
                .route("reaction-service-docs", r -> r
                        .path("/v3/api-docs/reaction/**", "/v3/api-docs/reaction")
                        .filters(f -> f.rewritePath("/v3/api-docs/reaction(?<segment>.*)", "/v3/api-docs${segment}"))
                        .uri("lb://REACTION-SERVICE"))
                .build();
    }
}
