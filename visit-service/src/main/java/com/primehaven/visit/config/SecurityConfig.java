package com.primehaven.visit.config;

import com.primehaven.visit.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final com.primehaven.visit.security.JsonSecurityErrorHandlers errorHandlers;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          com.primehaven.visit.security.JsonSecurityErrorHandlers errorHandlers) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.errorHandlers = errorHandlers;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/visits/**").hasRole("CLIENT")
                .requestMatchers(HttpMethod.DELETE, "/api/visits/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(errorHandlers)
                .accessDeniedHandler(errorHandlers))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
