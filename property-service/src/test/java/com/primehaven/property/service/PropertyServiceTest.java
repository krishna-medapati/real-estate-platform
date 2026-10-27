package com.primehaven.property.service;

import com.primehaven.property.config.ResourceNotFoundException;
import com.primehaven.property.dto.PropertyRequest;
import com.primehaven.property.dto.StatusUpdateRequest;
import com.primehaven.property.entity.Property;
import com.primehaven.property.entity.PropertyStatus;
import com.primehaven.property.repository.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock PropertyRepository repo;
    @InjectMocks PropertyService service;

    private PropertyRequest req() {
        PropertyRequest r = new PropertyRequest();
        r.setLocation("Hyderabad"); r.setPrice(5_000_000.0); r.setType("residential");
        return r;
    }

    @Test
    void create_setsAgentAndDefaultsToAvailable() {
        when(repo.save(any(Property.class))).thenAnswer(i -> i.getArgument(0));
        Property p = service.create(req(), "agent1");
        assertEquals("agent1", p.getAgentUsername());
        assertEquals(PropertyStatus.AVAILABLE, p.getStatus());
        assertEquals("Hyderabad", p.getLocation());
    }

    @Test
    void findById_missing_throwsNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void updateStatus_changesStatus() {
        Property existing = new Property();
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Property.class))).thenAnswer(i -> i.getArgument(0));
        StatusUpdateRequest s = new StatusUpdateRequest();
        s.setStatus(PropertyStatus.SOLD);
        assertEquals(PropertyStatus.SOLD, service.updateStatus(1L, s).getStatus());
    }

    @Test
    void delete_missing_throwsAndDeletesNothing() {
        when(repo.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(5L));
        verify(repo, never()).delete(any());
    }
}
