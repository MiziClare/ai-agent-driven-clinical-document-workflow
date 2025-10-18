package com.example.dto;

import com.example.entity.po.Prescription;
import com.example.entity.po.Requisition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentsResponse {
    private Prescription prescription;
    private Requisition requisition;
}

