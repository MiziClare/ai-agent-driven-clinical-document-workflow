package com.example.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import com.example.service.IClientService;
import com.example.service.IPrescriptionService;
import com.example.service.IRequisitionService;
import com.example.entity.po.Client;
import com.example.entity.po.Prescription;
import com.example.entity.po.Requisition;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DocTools {

    // Prepare to persist data using the services
    private final IClientService clientService;
    private final IPrescriptionService prescriptionService;
    private final IRequisitionService requisitionService;

    @Tool(description = "Persist a new prescription into course_ehealth_prescription. " +
            "Pass empty strings for pharmacyName and pharmacyAddress at INIT.")
    public String savePrescription(
            @ToolParam(description = "Client ID", required = true) int clientId,
            @ToolParam(description = "Prescriber ID", required = true) String prescriberId,
            @ToolParam(description = "Medication name", required = true) String medicationName,
            @ToolParam(description = "Medication strength", required = true) String medicationStrength,
            @ToolParam(description = "Dosage form", required = true) String medicationForm,
            @ToolParam(description = "Dosage instructions", required = true) String dosageInstructions,
            @ToolParam(description = "Quantity", required = true) Integer quantity,
            @ToolParam(description = "Refills allowed", required = false) Integer refillsAllowed,
            @ToolParam(description = "Date prescribed (yyyy-MM-dd)", required = false) String datePrescribed,
            @ToolParam(description = "Expiry date (yyyy-MM-dd)", required = false) String expiryDate,
            @ToolParam(description = "Pharmacy name (pass empty string at INIT)", required = true) String pharmacyName,
            @ToolParam(description = "Pharmacy address (pass empty string at INIT)", required = true) String pharmacyAddress,
            @ToolParam(description = "Status (default NEW)", required = false) String status,
            @ToolParam(description = "Notes", required = false) String notes
    ) {
        Client client = clientService.getClientById(clientId);
        if (client == null) {
            return "Client not found: " + clientId;
        }

        Prescription p = new Prescription();
        p.setPrescriptionId(UUID.randomUUID().toString());
        p.setClientId(clientId);
        p.setPrescriberId(prescriberId);
        p.setMedicationName(medicationName);
        p.setMedicationStrength(medicationStrength);
        p.setMedicationForm(medicationForm);
        p.setDosageInstructions(dosageInstructions);
        p.setQuantity(quantity != null ? quantity : 30);
        p.setRefillsAllowed(refillsAllowed != null ? refillsAllowed : 0);
        p.setDatePrescribed(parseOrDefault(datePrescribed, LocalDate.now()));
        p.setExpiryDate(parseOrDefault(expiryDate, LocalDate.now().plusDays(90)));
        // INIT: empty string -> store null
        p.setPharmacyName(blankToNull(pharmacyName));
        p.setPharmacyAddress(blankToNull(pharmacyAddress));
        p.setStatus((status == null || status.isBlank()) ? "NEW" : status);
        p.setNotes(notes);

        prescriptionService.addPrescription(p);
        return "OK";
    }

    @Tool(description = "Persist a new requisition into course_ehealth_requisition. " +
            "Pass empty strings for labName and labAddress at INIT.")
    public String saveRequisition(
            @ToolParam(description = "Client ID", required = true) int clientId,
            @ToolParam(description = "Requester ID", required = true) String requesterId,
            @ToolParam(description = "Department", required = true) String department,
            @ToolParam(description = "Test type", required = true) String testType,
            @ToolParam(description = "Test code", required = true) String testCode,
            @ToolParam(description = "Clinical information", required = true) String clinicalInfo,
            @ToolParam(description = "Date requested (yyyy-MM-dd)", required = true) String dateRequested,
            @ToolParam(description = "Priority (Routine/Urgent)", required = true) String priority,
            @ToolParam(description = "Status (default NEW)", required = false) String status,
            @ToolParam(description = "Lab name (pass empty string at INIT)", required = true) String labName,
            @ToolParam(description = "Lab address (pass empty string at INIT)", required = true) String labAddress,
            @ToolParam(description = "Result date (yyyy-MM-dd)", required = false) String resultDate,
            @ToolParam(description = "Notes", required = false) String notes
    ) {
        Client client = clientService.getClientById(clientId);
        if (client == null) {
            return "Client not found: " + clientId;
        }

        Requisition r = new Requisition();
        r.setRequisitionId(UUID.randomUUID().toString());
        r.setClientId(clientId);
        r.setRequesterId(requesterId);
        r.setDepartment(department);
        r.setTestType(testType);
        r.setTestCode(testCode);
        r.setClinicalInfo(clinicalInfo);
        r.setDateRequested(parseOrDefault(dateRequested, LocalDate.now()));
        r.setPriority(priority);
        r.setStatus((status == null || status.isBlank()) ? "NEW" : status);
        // INIT: empty string -> store null
        r.setLabName(blankToNull(labName));
        r.setLabAddress(blankToNull(labAddress));
        r.setResultDate(parseOrNull(resultDate));
        r.setNotes(notes);

        requisitionService.addRequisition(r);
        return "OK";
    }

    // --- helpers ---
    private static Date parseOrDefault(String yyyyMMdd, LocalDate def) {
        if (yyyyMMdd == null || yyyyMMdd.isBlank()) {
            return asDate(def);
        }
        try {
            return asDate(LocalDate.parse(yyyyMMdd));
        } catch (DateTimeParseException ex) {
            return asDate(def);
        }
    }

    private static Date parseOrNull(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.isBlank()) return null;
        try {
            return asDate(LocalDate.parse(yyyyMMdd));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
