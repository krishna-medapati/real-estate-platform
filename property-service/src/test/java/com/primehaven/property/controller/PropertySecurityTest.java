package com.primehaven.property.controller;

import com.primehaven.property.config.GlobalExceptionHandler;
import com.primehaven.property.config.ResourceNotFoundException;
import com.primehaven.property.config.SecurityConfig;
import com.primehaven.property.entity.Property;
import com.primehaven.property.security.JsonSecurityErrorHandlers;
import com.primehaven.property.security.JwtAuthFilter;
import com.primehaven.property.security.JwtUtil;
import com.primehaven.property.service.PropertyService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PropertyController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class,
        JsonSecurityErrorHandlers.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "jwt.secret=" + PropertySecurityTest.SECRET)
class PropertySecurityTest {

    static final String SECRET = "PrimeHaven-SOA-2026-Super-Secret-Signing-Key-Change-Me-In-Prod";
    static final String BODY = "{\"location\":\"Hyderabad\",\"price\":5000000,\"type\":\"residential\"}";

    @Autowired MockMvc mvc;
    @MockBean PropertyService propertyService;

    private String token(String user, String role, long expiresInMs) {
        return "Bearer " + Jwts.builder()
                .setSubject(user).claim("role", role)
                .setExpiration(new Date(System.currentTimeMillis() + expiresInMs))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void noToken_returns401() throws Exception {
        mvc.perform(get("/api/properties")).andExpect(status().isUnauthorized());
    }

    @Test
    void garbageToken_returns401() throws Exception {
        mvc.perform(get("/api/properties").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredToken_returns401() throws Exception {
        mvc.perform(get("/api/properties").header("Authorization", token("u", "CLIENT", -60_000)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void client_canListProperties() throws Exception {
        when(propertyService.findAll()).thenReturn(List.of());
        mvc.perform(get("/api/properties").header("Authorization", token("c", "CLIENT", 60_000)))
                .andExpect(status().isOk());
    }

    @Test
    void client_cannotCreateProperty_403() throws Exception {
        mvc.perform(post("/api/properties").header("Authorization", token("c", "CLIENT", 60_000))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void agent_canCreateProperty() throws Exception {
        when(propertyService.create(any(), anyString())).thenReturn(new Property());
        mvc.perform(post("/api/properties").header("Authorization", token("a", "AGENT", 60_000))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
    }

    @Test
    void agent_invalidBody_returns400() throws Exception {
        mvc.perform(post("/api/properties").header("Authorization", token("a", "AGENT", 60_000))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"\",\"price\":-5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingProperty_returns404() throws Exception {
        when(propertyService.findById(anyLong())).thenThrow(new ResourceNotFoundException("Property not found: 9"));
        mvc.perform(get("/api/properties/9").header("Authorization", token("c", "CLIENT", 60_000)))
                .andExpect(status().isNotFound());
    }
}
