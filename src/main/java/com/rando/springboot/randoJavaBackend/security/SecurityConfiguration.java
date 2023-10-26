package com.rando.springboot.randoJavaBackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable())
                .authorizeRequests()
                .requestMatchers(mvcMatcher("/api/**")).permitAll()
                .requestMatchers(mvcMatcher("/actuator/metrics")).permitAll()
                .requestMatchers(mvcMatcher("/ws/chatRoomMessages/**")).permitAll()
                .requestMatchers(mvcMatcher("/ws/**")).permitAll();

        return http.build();
    }


    @Autowired
    private HandlerMappingIntrospector introspector;

    private MvcRequestMatcher mvcMatcher(String pattern) {
        return new MvcRequestMatcher(introspector, pattern);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

