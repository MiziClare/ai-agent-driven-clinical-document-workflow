package com.example.service;

import com.example.dto.*;
import com.example.entity.po.Client;
import com.example.entity.po.Prescription;
import com.example.entity.po.Requisition;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

    private final IClientService clientService;
    private final IPrescriptionService prescriptionService;
    private final IRequisitionService requisitionService;
    @Qualifier("serviceChatClient")
    private final ChatClient serviceChatClient;
    private final WebClient googleWebClient;

    @Value("${google.maps.api.key:${google.maps.api-key:}}")
    private String googleApiKey;

    // INIT: return client info; if no latest documents exist, generate and persist random ones
    public InitResponse init(Integer clientId) {
        Client client = ensureClient(clientId);

        // If no documents yet, ask agent (tools) to create; fallback to local
        Prescription latestP = prescriptionService.getLatestPrescriptionByClientId(clientId);
        Requisition latestR = requisitionService.getLatestRequisitionByClientId(clientId);
        if (latestP == null || latestR == null) {
            try {
                String prompt = buildInitToolPrompt(clientId, latestP == null, latestR == null);
                serviceChatClient
                        .prompt()
                        .user(prompt)
                        .call()
                        .content();
            } catch (Exception e) {
                // fallback to local generators
                if (latestP == null) {
                    Prescription p = generateRandomPrescription(client);
                    prescriptionService.addPrescription(p);
                }
                if (latestR == null) {
                    Requisition r = generateRandomRequisition(client);
                    requisitionService.addRequisition(r);
                }
            }
        }

        return new InitResponse(client);
    }

    // GET_DOCUMENTS: latest Prescription & Requisition
    public DocumentsResponse getDocuments(Integer clientId) {
        ensureClient(clientId);
        Prescription p = prescriptionService.getLatestPrescriptionByClientId(clientId);
        Requisition r = requisitionService.getLatestRequisitionByClientId(clientId);
        if (p == null && r == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No documents found for client");
        }
        return new DocumentsResponse(p, r);
    }

    // FIND_NEARBY: call Google Maps Geocode + Nearby Search in parallel and return 3 pharmacies + 3 labs
    public NearbyResult findNearby(Integer clientId) {
        log.info("FIND_NEARBY request received: clientId={}", clientId);

        Client client;
        try {
            client = ensureClient(clientId);
            log.info("Client found: id={}, name='{}', address='{}'", client.getClientId(), client.getFirstName(), client.getAddress());
        } catch (Exception e) {
            log.error("Failed to find client: clientId={}, error={}", clientId, e.getMessage());
            throw e;
        }

        if (isBlank(client.getAddress())) {
            log.warn("Client address is empty: clientId={}", clientId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client address is empty");
        }

        // Log API key presence (not full key) for google maps calls
        boolean hasApiKey = !isBlank(googleApiKey);
        log.info("FIND_NEARBY start: clientId={}, address='{}', apiKeyPresent={}", clientId, client.getAddress(), hasApiKey);
        if (hasApiKey) {
            log.info("API Key length: {}, starts with: {}...", googleApiKey.length(),
                    googleApiKey.length() > 10 ? googleApiKey.substring(0, 10) : googleApiKey);
        }

        // If Google API Key is missing, use fallback immediately
        if (!hasApiKey) {
            log.warn("Google Maps API Key is not configured, using fallback data");
            return fallbackNearby(client.getAddress());
        }

        try {
            // Create independent Monos that handle their own errors gracefully
            Mono<List<PlaceCandidate>> pharmaciesMono = geocode(client.getAddress())
                    .flatMap(geo -> {
                        log.debug("Nearby search type=pharmacy at {},{}", geo.lat(), geo.lng());
                        return nearbyTopN(geo, "pharmacy", 3);
                    })
                    .onErrorReturn(Collections.emptyList())
                    .doOnError(ex -> log.debug("Pharmacy search failed, will use fallback", ex));

            Mono<List<PlaceCandidate>> labsMono = geocode(client.getAddress())
                    .flatMap(geo -> {
                        log.debug("Nearby search type=medical_lab at {},{}", geo.lat(), geo.lng());
                        return nearbyTopN(geo, "medical_lab", 3)
                                .flatMap(list -> {
                                    log.debug("Nearby medical_lab results={}", list.size());
                                    return list.isEmpty() ? nearbyTextSearchTopN(geo, "lab", 3) : Mono.just(list);
                                });
                    })
                    .onErrorReturn(Collections.emptyList())
                    .doOnError(ex -> log.debug("Lab search failed, will use fallback", ex));

            var tuple = Mono.zip(pharmaciesMono, labsMono)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (tuple == null) {
                log.error("Upstream timeout for clientId={}", clientId);
                return fallbackNearby(client.getAddress());
            }

            List<PlaceCandidate> phs = tuple.getT1();
            List<PlaceCandidate> lbs = tuple.getT2();

            // If either list is empty (due to API errors), use fallback
            if (phs.isEmpty() || lbs.isEmpty()) {
                log.info("API returned empty results, using fallback data");
                return fallbackNearby(client.getAddress());
            }

            log.info("FIND_NEARBY done: pharmacies={}, labs={}", phs.size(), lbs.size());
            return new NearbyResult(phs, lbs);

        } catch (Exception ex) {
            log.error("FIND_NEARBY unexpected error, using fallback", ex);
            return fallbackNearby(client.getAddress());
        }
    }

    // SAVE_SELECTION: update latest Prescription & Requisition with chosen pharmacy & lab
    public DocumentsResponse saveSelection(SelectionRequest request) {
        Integer clientId = request.getClientId();
        ensureClient(clientId);
        Prescription p = prescriptionService.getLatestPrescriptionByClientId(clientId);
        Requisition r = requisitionService.getLatestRequisitionByClientId(clientId);
        if (p == null || r == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing documents to update selection");
        }

        if (request.getPharmacyName() != null) p.setPharmacyName(request.getPharmacyName());
        if (request.getPharmacyAddress() != null) p.setPharmacyAddress(request.getPharmacyAddress());
        prescriptionService.updatePrescription(p);

        if (request.getLabName() != null) r.setLabName(request.getLabName());
        if (request.getLabAddress() != null) r.setLabAddress(request.getLabAddress());
        requisitionService.updateRequisition(r);

        return new DocumentsResponse(p, r);
    }

    // SEND_FAX: simulate fax to chosen pharmacy & lab
    public FaxResponse sendFax(Integer clientId) {
        ensureClient(clientId);
        Prescription p = prescriptionService.getLatestPrescriptionByClientId(clientId);
        Requisition r = requisitionService.getLatestRequisitionByClientId(clientId);
        if (p == null || r == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Documents not ready");
        }
        if (isBlank(p.getPharmacyName()) || isBlank(p.getPharmacyAddress()) ||
            isBlank(r.getLabName()) || isBlank(r.getLabAddress())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pharmacy/Lab selection incomplete");
        }
        String msg = String.format("Successfully sent fax to pharmacy [%s, %s] and laboratory [%s, %s].",
                p.getPharmacyName(), p.getPharmacyAddress(), r.getLabName(), r.getLabAddress());
        return new FaxResponse(true, msg);
    }

    // ----- helpers -----

    // Ensure client exists; throw 400/404 otherwise
    private Client ensureClient(Integer clientId) {
        log.debug("Checking client existence: clientId={}", clientId);

        if (clientId == null) {
            log.warn("clientId is null");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }

        Client c;
        try {
            c = clientService.getClientById(clientId);
            log.debug("Client service returned: {}", c != null ? "found" : "null");
        } catch (Exception e) {
            log.error("Database error when fetching client: clientId={}", clientId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }

        if (c == null) {
            log.warn("Client not found: clientId={}", clientId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found");
        }

        return c;
    }

    // Fallback random generators for INIT when agent tools fail - Prescription
    private Prescription generateRandomPrescription(Client client) {
        String[] meds = {"Amoxicillin", "Ibuprofen", "Metformin", "Lisinopril", "Atorvastatin"};
        String[] strengths = {"250mg", "500mg", "5mg", "10mg", "20mg"};
        String[] forms = {"Tablet", "Capsule", "Solution", "Suspension"};
        String[] instructions = {"Take one tablet twice daily after meals", "Take one capsule every 8 hours as needed for pain",
                "Take once daily with breakfast", "Take once daily in the morning"};

        Random rnd = new Random();
        String med = meds[rnd.nextInt(meds.length)];
        String strength = strengths[rnd.nextInt(strengths.length)];
        String form = forms[rnd.nextInt(forms.length)];
        String instr = instructions[rnd.nextInt(instructions.length)];
        int quantity = 10 + rnd.nextInt(40); // 10-49
        int refills = rnd.nextInt(3); // 0-2

        LocalDate today = LocalDate.now();
        Date datePrescribed = asDate(today);
        Date expiryDate = asDate(today.plusDays(90));

        Prescription p = new Prescription();
        p.setPrescriptionId(UUID.randomUUID().toString());
        p.setClientId(client.getClientId());
        p.setPrescriberId("DR-" + (1000 + rnd.nextInt(9000)));
        p.setMedicationName(med);
        p.setMedicationStrength(strength);
        p.setMedicationForm(form);
        p.setDosageInstructions(instr);
        p.setQuantity(quantity);
        p.setRefillsAllowed(refills);
        p.setDatePrescribed(datePrescribed);
        p.setExpiryDate(expiryDate);
        p.setPharmacyName(null); // initially empty per requirement
        p.setPharmacyAddress(null);
        p.setStatus("NEW");
        p.setNotes("Auto-generated for demo");
        return p;
    }

    // Fallback random generators for INIT when agent tools fail - Requisition
    private Requisition generateRandomRequisition(Client client) {
        String[] departments = {"Hematology", "Biochemistry", "Microbiology", "Radiology"};
        String[] testTypes = {"CBC", "CMP", "HbA1c", "Lipid Panel", "Thyroid Panel"};
        String[] priorities = {"Routine", "Urgent"};

        Random rnd = new Random();
        String dept = departments[rnd.nextInt(departments.length)];
        String test = testTypes[rnd.nextInt(testTypes.length)];
        String code = test.toUpperCase().replace(" ", "_") + "-" + (100 + rnd.nextInt(900));
        String clinicalInfo = "Assessment for baseline labs";

        LocalDate today = LocalDate.now();
        Date dateRequested = asDate(today);

        Requisition r = new Requisition();
        r.setRequisitionId(UUID.randomUUID().toString());
        r.setClientId(client.getClientId());
        r.setRequesterId("DR-" + (1000 + rnd.nextInt(9000)));
        r.setDepartment(dept);
        r.setTestType(test);
        r.setTestCode(code);
        r.setClinicalInfo(clinicalInfo);
        r.setDateRequested(dateRequested);
        r.setPriority(priorities[rnd.nextInt(priorities.length)]);
        r.setStatus("NEW");
        r.setLabName(null); // initially empty
        r.setLabAddress(null);
        r.setResultDate(null);
        r.setNotes("Auto-generated for demo");
        return r;
    }

    // Convert LocalDate to Date
    private static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // Check if string is null or blank
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // --- nearby fallback helpers ---
    private NearbyResult fallbackNearby(String addr) {
        log.info("Using fallback nearby data for address: {}", addr);
        List<PlaceCandidate> pharmacies = List.of(
                new PlaceCandidate("Central Pharmacy", maskedAddress(addr, "Downtown District"), 45.4215, -75.6993, 4.3),
                new PlaceCandidate("HealthPlus Pharmacy", maskedAddress(addr, "Medical Center"), 45.4205, -75.6983, 4.1),
                new PlaceCandidate("City Drug Store", maskedAddress(addr, "Shopping Mall"), 45.4225, -75.7003, 3.9)
        );
        List<PlaceCandidate> labs = List.of(
                new PlaceCandidate("Ottawa Medical Lab", maskedAddress(addr, "Hospital Complex"), 45.4210, -75.6988, 4.2),
                new PlaceCandidate("LifeLabs", maskedAddress(addr, "Clinic Building"), 45.4200, -75.6978, 4.0),
                new PlaceCandidate("Gamma-Dynacare", maskedAddress(addr, "Health Center"), 45.4220, -75.6998, 3.8)
        );
        return new NearbyResult(pharmacies, labs);
    }

    private static String maskedAddress(String base, String suffix) {
        if (isBlank(base)) return "Near " + suffix;
        // Extract city/region if possible
        String[] parts = base.split(",");
        String cityPart = parts.length > 1 ? parts[parts.length - 1].trim() : base;
        int idx = Math.min(8, cityPart.length());
        return cityPart.substring(0, idx) + "..., " + suffix;
    }

    // Build concise English prompt for agent to call the tools
    private String buildInitToolPrompt(Integer clientId, boolean needPrescription, boolean needRequisition) {
        return String.join("\n",
                "Task: Initialize e-health documents for the specified patient.",
                "Patient: clientId = " + clientId + ".",
                "Actions:",
                (needPrescription ? "- Create exactly one Prescription with realistic but generic medical content.\n" : "")
                        + (needRequisition ? "- Create exactly one Requisition with realistic but generic medical content.\n" : ""),
                "Requirements:",
                "- The fields must conform to tables course_ehealth_prescription and course_ehealth_requisition.",
                "- Set pharmacy_name and pharmacy_address as empty strings.",
                "- Set lab_name and lab_address as empty strings.",
                "- Use ISO-8601 dates when needed (yyyy-MM-dd).",
                "- Avoid PHI/PII; no markdown; do not echo the prompt.",
                "Tools to use:",
                "- savePrescription(clientId, prescriberId, medicationName, medicationStrength, medicationForm, dosageInstructions, quantity, refillsAllowed, datePrescribed, expiryDate, pharmacyName, pharmacyAddress, status, notes)",
                "- saveRequisition(clientId, requesterId, department, testType, testCode, clinicalInfo, dateRequested, priority, status, labName, labAddress, resultDate, notes)",
                "Output: Reply with OK after the tools have completed."
        );
    }

    // ---- Google Maps helpers ----

    private record Geo(double lat, double lng) {}

    private Mono<Geo> geocode(String address) {
        log.debug("Geocoding address='{}', apiKey present={}", address, !isBlank(googleApiKey));

        // Log full URL with masked API key for debugging
        String fullUrl = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                address.replace(" ", "%20"), googleApiKey);
        log.info("Geocoding URL (masked): https://maps.googleapis.com/maps/api/geocode/json?address={}...&key={}...",
                address.replace(" ", "%20"), googleApiKey.length() > 10 ? googleApiKey.substring(0, 10) : googleApiKey);

        return googleWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/maps/api/geocode/json")
                        .queryParam("address", address)
                        .queryParam("key", googleApiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(json -> {
                    String status = json.path("status").asText();
                    int results = json.path("results").size();
                    log.debug("Geocode response: status={}, results={}", status, results);

                    // If REQUEST_DENIED, log detailed troubleshooting info
                    if ("REQUEST_DENIED".equals(status)) {
                        String errorMessage = json.path("error_message").asText("No error message");
                        log.error("Google Maps API REQUEST_DENIED - Error message: {}", errorMessage);
                        log.error("Please check:");
                        log.error("1. API Key is valid and not expired");
                        log.error("2. Geocoding API is enabled in Google Cloud Console");
                        log.error("3. Billing is enabled for the project");
                        log.error("4. API Key has no IP/domain restrictions preventing server access");
                    }
                })
                .map(json -> {
                    String status = json.path("status").asText();
                    int results = json.path("results").size();
                    log.debug("Geocode status={}, results={}", status, results);
                    if (!"OK".equals(status)) {
                        log.error("Geocoding failed: status={}, address='{}'", status, address);
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geocoding failed: " + status);
                    }
                    JsonNode loc = json.path("results").path(0).path("geometry").path("location");
                    if (loc.isMissingNode()) {
                        log.error("Geocoding result missing location data");
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geocoding result missing");
                    }
                    return new Geo(loc.path("lat").asDouble(), loc.path("lng").asDouble());
                });
    }

    // Nearby Search with type + rankby=distance, then take top N
    private Mono<List<PlaceCandidate>> nearbyTopN(Geo geo, String type, int topN) {
        return googleWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/maps/api/place/nearbysearch/json")
                        .queryParam("location", geo.lat() + "," + geo.lng())
                        .queryParam("rankby", "distance")
                        .queryParam("type", type)
                        .queryParam("key", googleApiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> mapPlaces(json, topN))
                .onErrorReturn(Collections.emptyList());
    }

    // Fallback: Text Search for query near lat,lng
    private Mono<List<PlaceCandidate>> nearbyTextSearchTopN(Geo geo, String query, int topN) {
        return googleWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/maps/api/place/textsearch/json")
                        .queryParam("query", query)
                        .queryParam("location", geo.lat() + "," + geo.lng())
                        .queryParam("radius", 5000)
                        .queryParam("key", googleApiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> mapPlaces(json, topN))
                .onErrorReturn(Collections.emptyList());
    }

    private List<PlaceCandidate> mapPlaces(JsonNode json, int topN) {
        String status = json.path("status").asText();
        int total = json.path("results").size();
        log.debug("Places status={}, results(total)={}, topN={}", status, total, topN);
        if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
            log.warn("Places search failed: status={}, returning empty list", status);
            return Collections.emptyList();
        }
        List<PlaceCandidate> list = new ArrayList<>();
        JsonNode results = json.path("results");
        for (int i = 0; i < Math.min(topN, results.size()); i++) {
            JsonNode item = results.get(i);
            String name = item.path("name").asText("N/A");
            String address = item.has("vicinity")
                    ? item.path("vicinity").asText("N/A")
                    : item.path("formatted_address").asText("N/A");
            JsonNode loc = item.path("geometry").path("location");
            double lat = loc.path("lat").asDouble(0);
            double lng = loc.path("lng").asDouble(0);
            double rating = item.path("rating").asDouble(0);
            list.add(new PlaceCandidate(name, address, lat, lng, rating));
        }
        return list;
    }
}
