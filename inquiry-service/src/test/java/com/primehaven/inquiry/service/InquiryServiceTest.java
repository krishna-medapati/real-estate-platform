package com.primehaven.inquiry.service;

import com.primehaven.inquiry.client.PropertyClient;
import com.primehaven.inquiry.client.PropertyDto;
import com.primehaven.inquiry.client.VisitClient;
import com.primehaven.inquiry.client.VisitDto;
import com.primehaven.inquiry.config.ResourceNotFoundException;
import com.primehaven.inquiry.dto.InquiryRequest;
import com.primehaven.inquiry.dto.InquiryResponse;
import com.primehaven.inquiry.entity.Inquiry;
import com.primehaven.inquiry.repository.InquiryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock InquiryRepository inquiryRepository;
    @Mock PropertyClient propertyClient;
    @Mock VisitClient visitClient;
    @InjectMocks InquiryService service;

    private InquiryRequest request() {
        InquiryRequest r = new InquiryRequest();
        r.setPropertyId(1L); r.setMessage("Is it negotiable?");
        return r;
    }

    @Test
    void createInquiry_validatesPropertyThenAttachesVisits() {
        VisitDto visit = new VisitDto();
        visit.setVisitId(10L);
        when(propertyClient.getProperty(1L, "Bearer t")).thenReturn(new PropertyDto());
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(i -> i.getArgument(0));
        when(visitClient.getVisitsForProperty(1L, "Bearer t")).thenReturn(List.of(visit));

        InquiryResponse res = service.createInquiry(request(), "client1", "Bearer t");

        assertEquals("client1", res.getUserId());
        assertEquals(1, res.getRelatedVisits().size());
        var order = inOrder(propertyClient, inquiryRepository, visitClient);
        order.verify(propertyClient).getProperty(1L, "Bearer t");
        order.verify(inquiryRepository).save(any());
        order.verify(visitClient).getVisitsForProperty(1L, "Bearer t");
    }

    @Test
    void createInquiry_propertyNull_throwsAndSavesNothing() {
        when(propertyClient.getProperty(1L, "Bearer t")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.createInquiry(request(), "c", "Bearer t"));
        verify(inquiryRepository, never()).save(any());
    }

    @Test
    void createInquiry_property404_propagatesAndSavesNothing() {
        when(propertyClient.getProperty(1L, "Bearer t")).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "nf", HttpHeaders.EMPTY, null, null));
        assertThrows(HttpClientErrorException.class, () -> service.createInquiry(request(), "c", "Bearer t"));
        verify(inquiryRepository, never()).save(any());
    }

    @Test
    void findById_missing_notFound() {
        when(inquiryRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(3L));
    }
}
