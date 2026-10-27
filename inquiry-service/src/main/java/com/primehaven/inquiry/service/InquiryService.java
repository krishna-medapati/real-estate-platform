package com.primehaven.inquiry.service;

import com.primehaven.inquiry.client.PropertyClient;
import com.primehaven.inquiry.client.PropertyDto;
import com.primehaven.inquiry.client.VisitClient;
import com.primehaven.inquiry.dto.InquiryRequest;
import com.primehaven.inquiry.dto.InquiryResponse;
import com.primehaven.inquiry.entity.Inquiry;
import com.primehaven.inquiry.repository.InquiryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final PropertyClient propertyClient;
    private final VisitClient visitClient;

    public InquiryService(InquiryRepository inquiryRepository, PropertyClient propertyClient, VisitClient visitClient) {
        this.inquiryRepository = inquiryRepository;
        this.propertyClient = propertyClient;
        this.visitClient = visitClient;
    }

    public InquiryResponse createInquiry(InquiryRequest request, String username, String bearerToken) {
        // Inquiry -> Property: confirm the property is real before logging the inquiry
        PropertyDto property = propertyClient.getProperty(request.getPropertyId(), bearerToken);
        if (property == null) {
            throw new IllegalArgumentException("Property not found: " + request.getPropertyId());
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setUserId(username);
        inquiry.setPropertyId(request.getPropertyId());
        inquiry.setMessage(request.getMessage());
        inquiry = inquiryRepository.save(inquiry);

        // Property -> Visit: attach any scheduled visits for this property for context
        var relatedVisits = visitClient.getVisitsForProperty(request.getPropertyId(), bearerToken);

        return InquiryResponse.from(inquiry, relatedVisits);
    }

    public List<Inquiry> findAll() {
        return inquiryRepository.findAll();
    }

    public List<Inquiry> findByUser(String username) {
        return inquiryRepository.findByUserId(username);
    }

    public Inquiry findById(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new com.primehaven.inquiry.config.ResourceNotFoundException("Inquiry not found: " + id));
    }
}
