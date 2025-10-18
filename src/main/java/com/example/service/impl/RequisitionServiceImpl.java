package com.example.service.impl;

import com.example.entity.po.Requisition;
import com.example.service.IRequisitionService;
import com.example.mapper.RequisitionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequisitionServiceImpl implements IRequisitionService {

    @Autowired
    private RequisitionMapper requisitionMapper;

    @Override
    public Requisition getRequisitionById(String requisitionId) {
        return requisitionMapper.selectById(requisitionId);
    }

    @Override
    public List<Requisition> getRequisitionsByClientId(Integer clientId) {
        return requisitionMapper.selectByClientId(clientId);
    }

    @Override
    public int addRequisition(Requisition requisition) {
        return requisitionMapper.insert(requisition);
    }

    @Override
    public int updateRequisition(Requisition requisition) {
        return requisitionMapper.update(requisition);
    }

    @Override
    public int deleteRequisition(String requisitionId) {
        return requisitionMapper.delete(requisitionId);
    }

    @Override
    public Requisition getLatestRequisitionByClientId(Integer clientId) {
        return requisitionMapper.selectLatestByClientId(clientId);
    }
}
