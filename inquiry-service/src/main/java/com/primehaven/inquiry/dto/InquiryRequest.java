package com.primehaven.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InquiryRequest {

    @NotNull
    private Long propertyId;

    @NotBlank
    private String message;

    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
