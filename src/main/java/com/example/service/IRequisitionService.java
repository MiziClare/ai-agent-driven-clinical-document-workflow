package com.example.service;

import com.example.entity.po.Requisition;
import java.util.List;

public interface IRequisitionService {
    Requisition getRequisitionById(String requisitionId);
    List<Requisition> getRequisitionsByClientId(Integer clientId);
    int addRequisition(Requisition requisition);
    int updateRequisition(Requisition requisition);
    int deleteRequisition(String requisitionId);
    Requisition getLatestRequisitionByClientId(Integer clientId);
    int deleteAllRequisitionsByClientId(Integer clientId);
}
