package com.example.com.entity.po;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

// Requisition entity
@Data
public class Requisition implements Serializable {
    private String requisitionId;
    private Integer clientId;
    private String requesterId;
    private String department;
    private String testType;
    private String testCode;
    private String clinicalInfo;
    private Date dateRequested;
    private String priority;
    private String status;
    private String labName;
    private String labAddress;
    private Date resultDate;
    private String notes;
}
