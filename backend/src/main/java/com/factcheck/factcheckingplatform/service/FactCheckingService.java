package com.factcheck.factcheckingplatform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; // For making HTTP requests
import org.springframework.web.client.RestTemplate;

import com.factcheck.factcheckingplatform.model.Claim;
import com.factcheck.factcheckingplatform.repository.ClaimRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class FactCheckingService {

    @Autowired
    private ClaimRepository claimRepository;

    // RestTemplate for making HTTP requests (can be replaced by WebClient for reactive approach)
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON manipulation

    @PreAuthorize("hasRole('ROLE_USER')") // Only authenticated users can submit claims
    public Claim submitClaim(Claim claim) {
        claim.setSubmissionTimestamp(LocalDateTime.now());
        claim.setStatus("Pending"); // Default status upon submission
        return claimRepository.save(claim);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_FACT_CHECKER')") // Both roles can view all claims
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    @PreAuthorize("hasRole('ROLE_FACT_CHECKER')") // Only fact-checkers can update claims
    public Claim updateClaimStatusAndVerdict(Long id, String status, String verdict, String explanation, String verifiedBy) {
        return claimRepository.findById(id).map(claim -> {
            claim.setStatus(status);
            claim.setVerdict(verdict);
            claim.setExplanation(explanation);
            claim.setVerifiedBy(verifiedBy);
            claim.setVerificationTimestamp(LocalDateTime.now());
            return claimRepository.save(claim);
        }).orElseThrow(() -> new RuntimeException("Claim not found with id " + id));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_FACT_CHECKER')") // Both roles can view claims by status
    public List<Claim> getClaimsByStatus(String status) {
        return claimRepository.findByStatus(status);
    }
    // ... (existing code)

@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_FACT_CHECKER')") // Both roles can view claims by ID
public Optional<Claim> getClaimById(Long id) {
    return claimRepository.findById(id);
}

// ... (rest of the code)

    /**
     * Calls the Gemini API to analyze a claim and suggest a verdict and explanation.
     * @param claimContent The content of the claim to analyze.
     * @return A Map containing "verdict" and "explanation" suggested by Gemini.
     */
    @PreAuthorize("hasRole('ROLE_FACT_CHECKER')") // Only fact-checkers can trigger AI analysis
    public Map<String, String> analyzeClaimWithAI(String claimContent) {
        // --- Gemini API Configuration (Conceptual) ---
        // In a real application, the API key would be loaded from environment variables or a secure vault.
        // For Canvas, leaving it empty allows the platform to inject it.
        String apiKey = ""; // Canvas will provide this at runtime if empty
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        // --- Constructing the Gemini API Request Payload ---
        ObjectNode payload = objectMapper.createObjectNode();

        // chatHistory equivalent for contents
        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode userPart = objectMapper.createObjectNode();
        userPart.put("role", "user");

        ArrayNode parts = objectMapper.createArrayNode();
        parts.add(objectMapper.createObjectNode().put("text",
            "Fact-check the following claim. Provide a verdict (True, False, Misleading, Partially True) and a concise explanation. " +
            "Respond in JSON format with 'verdict' and 'explanation' fields. " +
            "Claim: " + claimContent
        ));
        userPart.set("parts", parts);
        contents.add(userPart);
        payload.set("contents", contents);

        // generationConfig for structured JSON output
        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("responseMimeType", "application/json");

        ObjectNode responseSchema = objectMapper.createObjectNode();
        responseSchema.put("type", "OBJECT");
        ObjectNode properties = objectMapper.createObjectNode();
        properties.put("verdict", objectMapper.createObjectNode().put("type", "STRING"));
        properties.put("explanation", objectMapper.createObjectNode().put("type", "STRING"));
        responseSchema.set("properties", properties);

        ArrayNode propertyOrdering = objectMapper.createArrayNode();
        propertyOrdering.add("verdict");
        propertyOrdering.add("explanation");
        responseSchema.set("propertyOrdering", propertyOrdering);

        generationConfig.set("responseSchema", responseSchema);
        payload.set("generationConfig", generationConfig);

        try {
            // --- Make the HTTP POST Request to Gemini API ---
            // RestTemplate expects a Map or Object for the body, and returns a String (JSON)
            String jsonPayload = objectMapper.writeValueAsString(payload);
            System.out.println("Sending to Gemini: " + jsonPayload); // For debugging

            // For simplicity, using RestTemplate.exchange. You might use WebClient for non-blocking.
            // The response from Gemini will be a JSON string.
            String geminiResponseJson = restTemplate.postForObject(apiUrl, payload, String.class);
            System.out.println("Received from Gemini: " + geminiResponseJson); // For debugging

            // --- Parse Gemini's Response ---
            // The response structure is result.candidates[0].content.parts[0].text
            ObjectNode rootNode = (ObjectNode) objectMapper.readTree(geminiResponseJson);
            if (rootNode.has("candidates") && rootNode.get("candidates").isArray() &&
                rootNode.get("candidates").get(0).has("content") &&
                rootNode.get("candidates").get(0).get("content").has("parts") &&
                rootNode.get("candidates").get(0).get("content").get("parts").isArray() &&
                rootNode.get("candidates").get(0).get("content").get("parts").get(0).has("text")) {

                String geminiOutputText = rootNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
                // The 'text' field itself contains the JSON string we requested
                Map<String, String> geminiVerdict = objectMapper.readValue(geminiOutputText, Map.class);
                return geminiVerdict;

            } else {
                System.err.println("Unexpected Gemini response structure: " + geminiResponseJson);
                return Map.of("verdict", "Unknown", "explanation", "AI analysis failed: Unexpected response structure.");
            }

        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            e.printStackTrace();
            return Map.of("verdict", "Unknown", "explanation", "AI analysis failed: " + e.getMessage());
        }
    }
}