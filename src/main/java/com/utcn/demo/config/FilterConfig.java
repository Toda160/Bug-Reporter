// src/main/java/com/utcn/demo/config/FilterConfig.java
package com.utcn.demo.config;

import com.utcn.demo.security.BannedUserFilter;
import com.utcn.demo.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public BannedUserFilter bannedUserFilter(UserService userService) {
        return new BannedUserFilter(userService);
    }
}
