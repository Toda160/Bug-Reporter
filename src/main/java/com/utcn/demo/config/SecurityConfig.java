package com.utcn.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/users/**").permitAll()
                .antMatchers("/tags/**").permitAll()
                .antMatchers("/api/bugs/**").permitAll()
                .antMatchers("/bug-tags/**").permitAll()
                .antMatchers("/moderation-actions/**").permitAll()
                .antMatchers("/api/comments/**").permitAll()
                .antMatchers("/api/votes/**").permitAll()
                .antMatchers("/bans/**").permitAll()  // ✅ Adaugă acces liber pentru /bans
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
