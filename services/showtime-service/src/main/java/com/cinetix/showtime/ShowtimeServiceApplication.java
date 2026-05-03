package com.cinetix.showtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableJpaRepositories(basePackages = {"com.cinetix.showtime", "com.cinetix.outbox"})
@EntityScan(basePackages = {"com.cinetix.showtime", "com.cinetix.outbox"})
public class ShowtimeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShowtimeServiceApplication.class, args);
    }
}
