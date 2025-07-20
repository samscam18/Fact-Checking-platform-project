package com.factcheck.factcheckingplatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.factcheck.factcheckingplatform.model.Claim;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByStatus(String status);
    // You could add more methods like findBySubmittedBy, findByVerdict, etc.
}
