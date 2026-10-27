package com.primehaven.inquiry.dto;

import com.primehaven.inquiry.client.VisitDto;
import com.primehaven.inquiry.entity.Inquiry;

import java.time.LocalDateTime;
import java.util.List;

public class InquiryResponse {
    private Long inquiryId;
    private String userId;
    private Long propertyId;
    private String message;
    private LocalDateTime createdAt;
    private List<VisitDto> relatedVisits;

    public static InquiryResponse from(Inquiry inquiry, List<VisitDto> relatedVisits) {
        InquiryResponse r = new InquiryResponse();
        r.inquiryId = inquiry.getInquiryId();
        r.userId = inquiry.getUserId();
        r.propertyId = inquiry.getPropertyId();
        r.message = inquiry.getMessage();
        r.createdAt = inquiry.getCreatedAt();
        r.relatedVisits = relatedVisits;
        return r;
    }

    public Long getInquiryId() { return inquiryId; }
    public String getUserId() { return userId; }
    public Long getPropertyId() { return propertyId; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<VisitDto> getRelatedVisits() { return relatedVisits; }
}
