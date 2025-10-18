package com.example.service;

import com.example.entity.po.Prescription;
import java.util.List;

public interface IPrescriptionService {
    Prescription getPrescriptionById(String prescriptionId);
    List<Prescription> getPrescriptionsByClientId(Integer clientId);
    int addPrescription(Prescription prescription);
    int updatePrescription(Prescription prescription);
    int deletePrescription(String prescriptionId);
    Prescription getLatestPrescriptionByClientId(Integer clientId);
}
