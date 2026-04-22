package com.cinetix.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    /** Rate-limit key by IP address. For authenticated users, use JWT subject instead. */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            var addr = exchange.getRequest().getRemoteAddress();
            String ip = addr != null ? addr.getAddress().getHostAddress() : "unknown";
            return Mono.just(ip);
        };
    }
}
