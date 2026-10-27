package com.primehaven.property.dto;

import com.primehaven.property.entity.PropertyStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {
    @NotNull
    private PropertyStatus status;

    public PropertyStatus getStatus() { return status; }
    public void setStatus(PropertyStatus status) { this.status = status; }
}
