package com.primehaven.inquiry.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
                SERVICE_URL, HttpMethod.GET, entity, PropertyDto.class, propertyId
        ).getBody();
    }
}
