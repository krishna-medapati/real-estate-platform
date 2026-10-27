package com.primehaven.visit.repository;

import com.primehaven.visit.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    List<Visit> findByUserId(String userId);
    List<Visit> findByPropertyId(Long propertyId);
}
