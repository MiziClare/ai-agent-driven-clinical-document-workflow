package com.example.controller;

import com.example.entity.po.Prescription;
import com.example.service.IPrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private IPrescriptionService prescriptionService;

    // GET /api/prescriptions - Get all prescriptions
    @GetMapping
    public List<Prescription> getAllPrescriptions() {
        return prescriptionService.getPrescriptionsByClientId(null);
    }

    // GET /api/prescriptions/{prescriptionId} - Get prescription by ID
    @GetMapping("/{prescriptionId}")
    public Prescription getPrescriptionById(@PathVariable("prescriptionId") String prescriptionId) {
        Prescription p = prescriptionService.getPrescriptionById(prescriptionId);
        if (p == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Prescription not found"
            );
        }
        return p;
    }

    // GET /api/prescriptions/client/{clientId} - Get prescriptions by client ID
    @GetMapping("/client/{clientId}")
    public List<Prescription> getPrescriptionsByClientId(@PathVariable("clientId") Integer clientId) {
        return prescriptionService.getPrescriptionsByClientId(clientId);
    }

    // GET /api/prescriptions/client/{clientId}/latest - Get latest prescription by client ID
    @GetMapping("/client/{clientId}/latest")
    public Prescription getLatestPrescriptionByClientId(@PathVariable("clientId") Integer clientId) {
        Prescription p = prescriptionService.getLatestPrescriptionByClientId(clientId);
        if (p == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "No prescription found for client"
            );
        }
        return p;
    }

    // POST /api/prescriptions - Add new prescription
    @PostMapping
    public int addPrescription(@RequestBody Prescription prescription) {
        return prescriptionService.addPrescription(prescription);
    }

    // PUT /api/prescriptions - Update existing prescription
    @PutMapping
    public int updatePrescription(@RequestBody Prescription prescription) {
        int result = prescriptionService.updatePrescription(prescription);
        if (result == 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Prescription not found"
            );
        }
        return result;
    }

    // DELETE /api/prescriptions/{prescriptionId} - Delete prescription by ID
    @DeleteMapping("/{prescriptionId}")
    public int deletePrescription(@PathVariable("prescriptionId") String prescriptionId) {
        int result = prescriptionService.deletePrescription(prescriptionId);
        if (result == 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Prescription not found"
            );
        }
        return result;
    }

    // DELETE /api/prescriptions/client/{clientId} - Delete all prescriptions by client ID
    @DeleteMapping("/client/{clientId}")
    public int deleteAllPrescriptionsByClientId(@PathVariable("clientId") Integer clientId) {
        int result = prescriptionService.deleteAllPrescriptionsByClientId(clientId);
        if (result == 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "No prescriptions found for client"
            );
        }
        return result;
    }
}
