package com.factcheck.factcheckingplatform.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String submittedBy; // User who submitted the claim
    private String content; // The actual text/claim to be checked
    private String sourceUrl; // URL where the claim was found
    private LocalDateTime submissionTimestamp;
    private String status; // e.g., "Pending", "Under Review", "Fact-Checked"
    private String verdict; // e.g., "True", "False", "Misleading", "Partially True"
    private String explanation; // Detailed explanation for the verdict
    private String verifiedBy; // Fact-checker who verified it
    private LocalDateTime verificationTimestamp;

    public Claim(String submittedBy, String content, String sourceUrl) {
        this.submittedBy = submittedBy;
        this.content = content;
        this.sourceUrl = sourceUrl;
        this.submissionTimestamp = LocalDateTime.now();
        this.status = "Pending"; // Initial status
    }
}
