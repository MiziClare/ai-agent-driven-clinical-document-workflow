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

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final IClientService clientService;
    private final IPrescriptionService prescriptionService;
    private final IRequisitionService requisitionService;
    @Qualifier("serviceChatClient")
    private final ChatClient serviceChatClient;

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

    // FIND_NEARBY: stubbed candidates (3 pharmacies + 3 labs)
    public NearbyResult findNearby(Integer clientId) {
        Client client = ensureClient(clientId);
        // Stub: fabricate 3 pharmacies & 3 labs. In a real impl, call Google Maps Places API by client.address
        List<PlaceCandidate> pharmacies = Arrays.asList(
                new PlaceCandidate("Community Pharmacy A", maskedAddress(client.getAddress(), "Unit 101"), 43.65107, -79.347015, 4.5),
                new PlaceCandidate("Wellness Pharmacy B", maskedAddress(client.getAddress(), "Suite 5"), 43.6532, -79.3832, 4.2),
                new PlaceCandidate("CityCare Pharmacy C", maskedAddress(client.getAddress(), "#12"), 43.645, -79.39, 4.6)
        );
        List<PlaceCandidate> labs = Arrays.asList(
                new PlaceCandidate("HealthLab One", maskedAddress(client.getAddress(), "Floor 2"), 43.6629, -79.3957, 4.1),
                new PlaceCandidate("QuickTest Diagnostics", maskedAddress(client.getAddress(), "Rm 203"), 43.67, -79.4, 4.3),
                new PlaceCandidate("Downtown Medical Labs", maskedAddress(client.getAddress(), "Building B"), 43.64, -79.38, 4.0)
        );
        return new NearbyResult(pharmacies, labs);
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
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        Client c = clientService.getClientById(clientId);
        if (c == null) {
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

    // Mask address for stub data
    private static String maskedAddress(String base, String suffix) {
        if (isBlank(base)) return "N/A";
        // light obfuscation to avoid echoing full address in stub data
        int idx = Math.min(6, base.length());
        return base.substring(0, idx) + "***, " + suffix;
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
}
