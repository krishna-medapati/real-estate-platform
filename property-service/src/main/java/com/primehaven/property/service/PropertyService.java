package com.primehaven.property.service;

import com.primehaven.property.dto.PropertyRequest;
import com.primehaven.property.dto.StatusUpdateRequest;
import com.primehaven.property.entity.Property;
import com.primehaven.property.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    public Property findById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new com.primehaven.property.config.ResourceNotFoundException("Property not found: " + id));
    }

    public Property create(PropertyRequest request, String agentUsername) {
        Property property = new Property();
        property.setLocation(request.getLocation());
        property.setPrice(request.getPrice());
        property.setType(request.getType());
        property.setAgentUsername(agentUsername);
        return propertyRepository.save(property);
    }

    public Property update(Long id, PropertyRequest request) {
        Property property = findById(id);
        property.setLocation(request.getLocation());
        property.setPrice(request.getPrice());
        property.setType(request.getType());
        return propertyRepository.save(property);
    }

    public Property updateStatus(Long id, StatusUpdateRequest request) {
        Property property = findById(id);
        property.setStatus(request.getStatus());
        return propertyRepository.save(property);
    }

    public void delete(Long id) {
        Property property = findById(id);
        propertyRepository.delete(property);
    }
}
