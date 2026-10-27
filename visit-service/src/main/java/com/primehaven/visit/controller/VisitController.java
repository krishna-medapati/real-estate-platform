package com.primehaven.visit.controller;

import com.primehaven.visit.dto.VisitRequest;
import com.primehaven.visit.entity.Visit;
import com.primehaven.visit.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
@CrossOrigin(origins = "*")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    public ResponseEntity<Visit> schedule(@Valid @RequestBody VisitRequest request,
                                           Authentication auth,
                                           @RequestHeader("Authorization") String bearerToken) {
        return ResponseEntity.ok(visitService.scheduleVisit(request, auth.getName(), bearerToken));
    }

    @GetMapping
    public ResponseEntity<List<Visit>> getAll(Authentication auth) {
        boolean isAgent = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"));
        if (isAgent) {
            return ResponseEntity.ok(visitService.findAll());
        }
        return ResponseEntity.ok(visitService.findByUser(auth.getName()));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<Visit>> getByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(visitService.findByProperty(propertyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Visit> getById(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, Authentication auth) {
        boolean isAgent = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"));
        visitService.cancel(id, auth.getName(), isAgent);
        return ResponseEntity.noContent().build();
    }
}
