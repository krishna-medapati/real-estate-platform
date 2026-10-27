package com.primehaven.visit.service;

import com.primehaven.visit.client.PropertyClient;
import com.primehaven.visit.client.PropertyDto;
import com.primehaven.visit.config.ResourceNotFoundException;
import com.primehaven.visit.dto.VisitRequest;
import com.primehaven.visit.entity.Visit;
import com.primehaven.visit.entity.VisitStatus;
import com.primehaven.visit.repository.VisitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    @Mock VisitRepository visitRepository;
    @Mock PropertyClient propertyClient;
    @InjectMocks VisitService service;

    private VisitRequest request(LocalDateTime when) {
        VisitRequest r = new VisitRequest();
        r.setPropertyId(1L); r.setVisitDate(when);
        return r;
    }

    private PropertyDto property(String status) {
        PropertyDto p = new PropertyDto();
        p.setPropertyId(1L); p.setStatus(status);
        return p;
    }

    private Visit visit(String owner, VisitStatus st) {
        Visit v = new Visit();
        v.setUserId(owner); v.setStatus(st);
        return v;
    }

    @Test
    void schedule_availableProperty_savesScheduledVisit() {
        when(propertyClient.getProperty(1L, "Bearer t")).thenReturn(property("AVAILABLE"));
        when(visitRepository.save(any(Visit.class))).thenAnswer(i -> i.getArgument(0));
        Visit v = service.scheduleVisit(request(LocalDateTime.now().plusDays(2)), "client1", "Bearer t");
        assertEquals(VisitStatus.SCHEDULED, v.getStatus());
        assertEquals("client1", v.getUserId());
    }

    @Test
    void schedule_soldProperty_throwsConflict() {
        when(propertyClient.getProperty(1L, "Bearer t")).thenReturn(property("SOLD"));
        assertThrows(IllegalStateException.class,
                () -> service.scheduleVisit(request(LocalDateTime.now().plusDays(1)), "c", "Bearer t"));
        verify(visitRepository, never()).save(any());
    }

    @Test
    void schedule_pastDate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.scheduleVisit(request(LocalDateTime.now().minusDays(1)), "c", "Bearer t"));
        verifyNoInteractions(propertyClient);
    }

    @Test
    void schedule_propertyMissing_throws() {
        when(propertyClient.getProperty(1L, "Bearer t")).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.scheduleVisit(request(LocalDateTime.now().plusDays(1)), "c", "Bearer t"));
    }

    @Test
    void cancel_ownVisit_ok() {
        Visit v = visit("client1", VisitStatus.SCHEDULED);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(v));
        service.cancel(1L, "client1", false);
        assertEquals(VisitStatus.CANCELLED, v.getStatus());
        verify(visitRepository).save(v);
    }

    @Test
    void cancel_someoneElsesVisit_forbidden() {
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit("client1", VisitStatus.SCHEDULED)));
        assertThrows(AccessDeniedException.class, () -> service.cancel(1L, "hacker", false));
        verify(visitRepository, never()).save(any());
    }

    @Test
    void cancel_agentCanCancelAny() {
        Visit v = visit("client1", VisitStatus.SCHEDULED);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(v));
        service.cancel(1L, "agent1", true);
        assertEquals(VisitStatus.CANCELLED, v.getStatus());
    }

    @Test
    void cancel_alreadyCancelled_conflict() {
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit("c", VisitStatus.CANCELLED)));
        assertThrows(IllegalStateException.class, () -> service.cancel(1L, "c", false));
    }

    @Test
    void findById_missing_notFound() {
        when(visitRepository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(7L));
    }
}
