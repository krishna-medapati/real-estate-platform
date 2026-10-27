package com.primehaven.inquiry.controller;

import com.primehaven.inquiry.dto.InquiryRequest;
import com.primehaven.inquiry.dto.InquiryResponse;
import com.primehaven.inquiry.entity.Inquiry;
import com.primehaven.inquiry.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@CrossOrigin(origins = "*")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping
    public ResponseEntity<InquiryResponse> create(@Valid @RequestBody InquiryRequest request,
                                                    Authentication auth,
                                                    @RequestHeader("Authorization") String bearerToken) {
        return ResponseEntity.ok(inquiryService.createInquiry(request, auth.getName(), bearerToken));
    }

    @GetMapping
    public ResponseEntity<List<Inquiry>> getAll(Authentication auth) {
        boolean isAgent = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"));
        if (isAgent) {
            return ResponseEntity.ok(inquiryService.findAll());
        }
        return ResponseEntity.ok(inquiryService.findByUser(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inquiry> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inquiryService.findById(id));
    }
}
