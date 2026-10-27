package com.primehaven.visit.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class VisitRequest {

    @NotNull
    private Long propertyId;

    @NotNull
    private LocalDateTime visitDate;

    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
    public LocalDateTime getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }
}
