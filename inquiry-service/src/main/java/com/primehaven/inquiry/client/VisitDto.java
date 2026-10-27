package com.primehaven.inquiry.client;

public class VisitDto {
    private Long visitId;
    private Long propertyId;
    private String userId;
    private String visitDate;
    private String status;

    public Long getVisitId() { return visitId; }
    public void setVisitId(Long visitId) { this.visitId = visitId; }
    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
