package com.primehaven.inquiry.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Completes the "Inquiry -> Property -> Visit" chain from the spec: once an
 * inquiry's property has been validated, we also look up any scheduled
 * visits for that property so the agent has full context on the inquiry.
 */
@Component
public class VisitClient {

    private static final String SERVICE_URL = "http://VISIT-SERVICE/api/visits/property/{propertyId}";

    private final RestTemplate restTemplate;

    public VisitClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<VisitDto> getVisitsForProperty(Long propertyId, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            VisitDto[] visits = restTemplate.exchange(
                    SERVICE_URL, HttpMethod.GET, entity, VisitDto[].class, propertyId
            ).getBody();
            return visits == null ? List.of() : Arrays.asList(visits);
        } catch (Exception ex) {
            return List.of();
        }
    }
}
