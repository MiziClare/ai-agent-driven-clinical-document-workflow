package com.example.dto;

import lombok.Data;

@Data
public class SelectionRequest {
    private Integer clientId;
    private String pharmacyName;
    private String pharmacyAddress;
    private String labName;
    private String labAddress;
}

