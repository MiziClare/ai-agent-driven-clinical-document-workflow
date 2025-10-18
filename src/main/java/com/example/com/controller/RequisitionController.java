package com.example.com.controller;

import com.example.com.entity.po.Requisition;
import com.example.com.service.IRequisitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/requisitions")
public class RequisitionController {

    @Autowired
    private IRequisitionService requisitionService;

    // GET /api/requisitions - Get all requisitions
    @GetMapping
    public List<Requisition> getAllRequisitions() {
        // 若 mapper/服务不支持 null，可改为新增 getAll 方法
        return requisitionService.getRequisitionsByClientId(null);
    }

    // GET /api/requisitions/{requisitionId} - Get requisition by ID
    @GetMapping("/{requisitionId}")
    public Requisition getRequisitionById(@PathVariable("requisitionId") String requisitionId) {
        Requisition r = requisitionService.getRequisitionById(requisitionId);
        if (r == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Requisition not found"
            );
        }
        return r;
    }

    // GET /api/requisitions/client/{clientId} - Get requisitions by client ID
    @GetMapping("/client/{clientId}")
    public List<Requisition> getRequisitionsByClientId(@PathVariable("clientId") Integer clientId) {
        return requisitionService.getRequisitionsByClientId(clientId);
    }

    // GET /api/requisitions/client/{clientId}/latest - Get latest requisition by client ID
    @GetMapping("/client/{clientId}/latest")
    public Requisition getLatestRequisitionByClientId(@PathVariable("clientId") Integer clientId) {
        Requisition r = requisitionService.getLatestRequisitionByClientId(clientId);
        if (r == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "No requisition found for client"
            );
        }
        return r;
    }

    // POST /api/requisitions - Add new requisition
    @PostMapping
    public int addRequisition(@RequestBody Requisition requisition) {
        return requisitionService.addRequisition(requisition);
    }

    // PUT /api/requisitions - Update existing requisition
    @PutMapping
    public int updateRequisition(@RequestBody Requisition requisition) {
        int result = requisitionService.updateRequisition(requisition);
        if (result == 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Requisition not found"
            );
        }
        return result;
    }

    // DELETE /api/requisitions/{requisitionId} - Delete requisition by ID
    @DeleteMapping("/{requisitionId}")
    public int deleteRequisition(@PathVariable("requisitionId") String requisitionId) {
        int result = requisitionService.deleteRequisition(requisitionId);
        if (result == 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Requisition not found"
            );
        }
        return result;
    }
}
