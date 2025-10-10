package com.example.com.service;

import com.example.com.entity.po.Requisition;
import java.util.List;

public interface IRequisitionService {
    Requisition getRequisitionById(String requisitionId);
    List<Requisition> getRequisitionsByClientId(Integer clientId);
    int addRequisition(Requisition requisition);
    int updateRequisition(Requisition requisition);
    int deleteRequisition(String requisitionId);
}
