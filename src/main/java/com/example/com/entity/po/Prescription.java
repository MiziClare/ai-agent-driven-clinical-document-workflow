package com.example.com.entity.po;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

// Prescription entity
@Data
public class Prescription implements Serializable {
    private String prescriptionId;
    private Integer clientId;
    private String prescriberId;
    private String medicationName;
    private String medicationStrength;
    private String medicationForm;
    private String dosageInstructions;
    private Integer quantity;
    private Integer refillsAllowed;
    private Date datePrescribed;
    private Date expiryDate;
    private String pharmacyName;
    private String pharmacyAddress;
    private String status;
    private String notes;
}
