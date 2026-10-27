package com.primehaven.visit.service;

import com.primehaven.visit.client.PropertyClient;
import com.primehaven.visit.client.PropertyDto;
import com.primehaven.visit.dto.VisitRequest;
import com.primehaven.visit.entity.Visit;
import com.primehaven.visit.entity.VisitStatus;
import com.primehaven.visit.repository.VisitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitService {

    private final VisitRepository visitRepository;
    private final PropertyClient propertyClient;

    public VisitService(VisitRepository visitRepository, PropertyClient propertyClient) {
        this.visitRepository = visitRepository;
        this.propertyClient = propertyClient;
    }

    public Visit scheduleVisit(VisitRequest request, String username, String bearerToken) {
        if (request.getVisitDate().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Visit date must be in the future");
        }
        PropertyDto property = propertyClient.getProperty(request.getPropertyId(), bearerToken);

        if (property == null) {
            throw new IllegalArgumentException("Property not found: " + request.getPropertyId());
        }
        if (!"AVAILABLE".equals(property.getStatus())) {
            throw new IllegalStateException("Property is not available for a visit right now (status: " + property.getStatus() + ")");
        }

        Visit visit = new Visit();
        visit.setPropertyId(request.getPropertyId());
        visit.setUserId(username);
        visit.setVisitDate(request.getVisitDate());
        visit.setStatus(VisitStatus.SCHEDULED);
        return visitRepository.save(visit);
    }

    public List<Visit> findAll() {
        return visitRepository.findAll();
    }

    public List<Visit> findByUser(String username) {
        return visitRepository.findByUserId(username);
    }

    public List<Visit> findByProperty(Long propertyId) {
        return visitRepository.findByPropertyId(propertyId);
    }

    public Visit findById(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new com.primehaven.visit.config.ResourceNotFoundException("Visit not found: " + id));
    }

    public void cancel(Long id, String username, boolean isAgent) {
        Visit visit = findById(id);
        if (!isAgent && !visit.getUserId().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only cancel your own visits");
        }
        if (visit.getStatus() == VisitStatus.CANCELLED) {
            throw new IllegalStateException("Visit is already cancelled");
        }
        visit.setStatus(VisitStatus.CANCELLED);
        visitRepository.save(visit);
    }
}
