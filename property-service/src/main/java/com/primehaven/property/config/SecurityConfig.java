package com.primehaven.property.config;

import com.primehaven.property.security.JwtAuthFilter;
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
    private final com.primehaven.property.security.JsonSecurityErrorHandlers errorHandlers;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          com.primehaven.property.security.JsonSecurityErrorHandlers errorHandlers) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.errorHandlers = errorHandlers;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/properties/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/properties/**").hasRole("AGENT")
                .requestMatchers(HttpMethod.PUT, "/api/properties/**").hasRole("AGENT")
                .requestMatchers(HttpMethod.PATCH, "/api/properties/**").hasRole("AGENT")
                .requestMatchers(HttpMethod.DELETE, "/api/properties/**").hasRole("AGENT")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(errorHandlers)
                .accessDeniedHandler(errorHandlers))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
