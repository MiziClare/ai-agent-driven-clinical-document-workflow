package com.example.service.impl;

import com.example.entity.po.Prescription;
import com.example.mapper.PrescriptionMapper;
import com.example.service.IPrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PrescriptionServiceImpl implements IPrescriptionService {

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @Override
    public Prescription getPrescriptionById(String prescriptionId) {
        return prescriptionMapper.selectById(prescriptionId);
    }

    @Override
    public List<Prescription> getPrescriptionsByClientId(Integer clientId) {
        return prescriptionMapper.selectByClientId(clientId);
    }

    @Override
    public int addPrescription(Prescription prescription) {
        return prescriptionMapper.insert(prescription);
    }

    @Override
    public int updatePrescription(Prescription prescription) {
        return prescriptionMapper.update(prescription);
    }

    @Override
    public int deletePrescription(String prescriptionId) {
        return prescriptionMapper.delete(prescriptionId);
    }

    @Override
    public Prescription getLatestPrescriptionByClientId(Integer clientId) {
        return prescriptionMapper.selectLatestByClientId(clientId);
    }

    @Override
    public int deleteAllPrescriptionsByClientId(Integer clientId) {
        return prescriptionMapper.deleteAllPrescriptionsByClientId(clientId);
    }
}
