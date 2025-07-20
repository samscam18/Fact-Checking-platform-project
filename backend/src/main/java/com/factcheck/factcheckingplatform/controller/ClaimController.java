package com.factcheck.factcheckingplatform.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.factcheck.factcheckingplatform.model.Claim;
import com.factcheck.factcheckingplatform.service.FactCheckingService;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    @Autowired
    private FactCheckingService factCheckingService;

    @PostMapping
    public ResponseEntity<Claim> submitClaim(@RequestBody Claim claim) {
        // Get the authenticated user's username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String submittedBy = authentication.getName();
        claim.setSubmittedBy(submittedBy); // Set the submitter from authenticated user

        Claim savedClaim = factCheckingService.submitClaim(claim);
        return ResponseEntity.ok(savedClaim);
    }

    @GetMapping
    public List<Claim> getAllClaims() {
        return factCheckingService.getAllClaims();
    }

    @GetMapping("/status/{status}")
    public List<Claim> getClaimsByStatus(@PathVariable String status) {
        return factCheckingService.getClaimsByStatus(status);
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<Claim> verifyClaim(@PathVariable Long id, @RequestBody Map<String, String> verificationDetails) {
        String status = verificationDetails.get("status");
        String verdict = verificationDetails.get("verdict");
        String explanation = verificationDetails.get("explanation");

        // Get the authenticated fact-checker's username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String verifiedBy = authentication.getName();

        Claim updatedClaim = factCheckingService.updateClaimStatusAndVerdict(id, status, verdict, explanation, verifiedBy);
        return ResponseEntity.ok(updatedClaim);
    }

    /**
     * New endpoint to trigger AI analysis for a specific claim.
     * This will return AI's suggested verdict and explanation.
     */
    @PostMapping("/{id}/ai-check")
    public ResponseEntity<Map<String, String>> aiCheckClaim(@PathVariable Long id) {
        Optional<Claim> optionalClaim = factCheckingService.getClaimById(id); // <--- CORRECTED LINE
        if (optionalClaim.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String claimContent = optionalClaim.get().getContent();
        Map<String, String> aiSuggestion = factCheckingService.analyzeClaimWithAI(claimContent);
        return ResponseEntity.ok(aiSuggestion);
    }
}