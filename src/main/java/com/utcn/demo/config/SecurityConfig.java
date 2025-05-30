package com.utcn.demo.config;

import com.utcn.demo.security.BannedUserFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final BannedUserFilter bannedUserFilter;

    public SecurityConfig(BannedUserFilter bannedUserFilter) {
        this.bannedUserFilter = bannedUserFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .authorizeRequests()
                // Public endpoints (all endpoints are public as requested)
                .antMatchers("/**").permitAll()
                
                .anyRequest().permitAll()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            // Removing bannedUserFilter as all endpoints are permitted
            // .addFilterBefore(bannedUserFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
