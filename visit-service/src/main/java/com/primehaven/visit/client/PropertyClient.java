package com.primehaven.visit.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Calls property-service through Eureka + the load-balanced RestTemplate,
 * forwarding the caller's JWT so property-service's own security still applies.
 * This is the "Inquiry/Visit -> Property" inter-service hop from the spec.
 */
@Component
public class PropertyClient {

    private static final String SERVICE_URL = "http://PROPERTY-SERVICE/api/properties/{id}";

    private final RestTemplate restTemplate;

    public PropertyClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PropertyDto getProperty(Long propertyId, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                SERVICE_URL,
                HttpMethod.GET,
                entity,
                PropertyDto.class,
                propertyId
        ).getBody();
    }
}
