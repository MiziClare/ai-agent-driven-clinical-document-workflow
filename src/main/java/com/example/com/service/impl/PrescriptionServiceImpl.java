package com.example.com.service.impl;

import com.example.com.entity.po.Prescription;
import com.example.com.mapper.PrescriptionMapper;
import com.example.com.service.IPrescriptionService;
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
}
