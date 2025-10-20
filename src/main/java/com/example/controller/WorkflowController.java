package com.example.controller;

import com.example.dto.*;
import com.example.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ehealth")
public class WorkflowController {

    private static final Logger log = LoggerFactory.getLogger(WorkflowController.class);

    private final WorkflowService workflowService;
    private final WebClient googleWebClient;

    @Value("${google.maps.api.key:${google.maps.api-key:}}")
    private String googleApiKey;

    // Task 1. INIT: initialize workflow for a client
    @PostMapping("/init")
    public InitResponse init(@RequestParam("clientId") Integer clientId) {
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        return workflowService.init(clientId);
    }

    // Task2 2. GET_DOCUMENTS: return latest Prescription & Requisition for client
    @GetMapping("/documents")
    public DocumentsResponse getDocuments(@RequestParam("clientId") Integer clientId) {
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        return workflowService.getDocuments(clientId);
    }

    // Task 3. FIND_NEARBY: stubbed nearby search result for pharmacies & labs
    @GetMapping("/nearby")
    public NearbyResult findNearby(@RequestParam("clientId") Integer clientId) {
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        return workflowService.findNearby(clientId);
    }

    // Task 4. SAVE_SELECTION: persist chosen pharmacy & lab; return updated documents
    @PostMapping("/selection")
    public DocumentsResponse saveSelection(@RequestBody SelectionRequest request) {
        if (request == null || request.getClientId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        return workflowService.saveSelection(request);
    }

    // Task 5. SEND_FAX: simulate faxing documents to chosen pharmacy & lab
    @PostMapping("/send-fax")
    public FaxResponse sendFax(@RequestParam("clientId") Integer clientId) {
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        return workflowService.sendFax(clientId);
    }

    // Temporary: Debug endpoint to test Google Maps Geocoding API integration
    @GetMapping("/debug/google-api")
    public String debugGoogleApi(@RequestParam(defaultValue = "Ottawa, ON") String address) {
        log.info("Debug Google API called with address: {}", address);

        if (googleApiKey == null || googleApiKey.isBlank()) {
            return "ERROR: Google API Key is not configured";
        }

        try {
            JsonNode response = googleWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/maps/api/geocode/json")
                            .queryParam("address", address)
                            .queryParam("key", googleApiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) {
                return "ERROR: No response from Google API";
            }

            String status = response.path("status").asText();
            String errorMessage = response.path("error_message").asText("No error message");
            int results = response.path("results").size();

            StringBuilder result = new StringBuilder();
            result.append("Google Maps API Test Result:\n");
            result.append("Status: ").append(status).append("\n");
            result.append("Results count: ").append(results).append("\n");
            result.append("Error message: ").append(errorMessage).append("\n");
            result.append("API Key present: ").append(!googleApiKey.isBlank()).append("\n");
            result.append("API Key length: ").append(googleApiKey.length()).append("\n");

            if ("OK".equals(status) && results > 0) {
                JsonNode location = response.path("results").path(0).path("geometry").path("location");
                result.append("Coordinates: ").append(location.path("lat").asDouble())
                      .append(", ").append(location.path("lng").asDouble()).append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Google API test failed", e);
            return "ERROR: " + e.getMessage();
        }
    }
}
