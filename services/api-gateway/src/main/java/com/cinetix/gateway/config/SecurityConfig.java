package com.cinetix.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(auth -> auth
                // Public endpoints - no auth needed
                .pathMatchers("/api/v1/movies/**").permitAll()
                .pathMatchers("/api/v1/cinemas/**").permitAll()
                .pathMatchers("/api/v1/showtimes/**").permitAll()
                .pathMatchers("/api/v1/promotions/**").permitAll()
                .pathMatchers("/ws/**").permitAll()
                .pathMatchers("/actuator/health").permitAll()
                // Everything else requires authentication
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {}) // JWT config from application.yml
            )
            .build();
    }
}
