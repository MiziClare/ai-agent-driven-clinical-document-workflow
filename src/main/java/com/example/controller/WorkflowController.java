package com.example.controller;

import com.example.dto.*;
import com.example.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ehealth")
public class WorkflowController {

    private final WorkflowService workflowService;

    // INIT: initialize workflow for a client
    @PostMapping("/init")
    public InitResponse init(@RequestParam("clientId") Integer clientId) {
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        return workflowService.init(clientId);
    }

    // GET_DOCUMENTS: return latest Prescription & Requisition for client
    @GetMapping("/documents")
    public DocumentsResponse getDocuments(@RequestParam("clientId") Integer clientId) {
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        return workflowService.getDocuments(clientId);
    }

    // FIND_NEARBY: stubbed nearby search result for pharmacies & labs
    @GetMapping("/nearby")
    public NearbyResult findNearby(@RequestParam("clientId") Integer clientId) {
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        return workflowService.findNearby(clientId);
    }

    // SAVE_SELECTION: persist chosen pharmacy & lab; return updated documents
    @PostMapping("/selection")
    public DocumentsResponse saveSelection(@RequestBody SelectionRequest request) {
        if (request == null || request.getClientId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        return workflowService.saveSelection(request);
    }

    // SEND_FAX: simulate faxing documents to chosen pharmacy & lab
    @PostMapping("/send-fax")
    public FaxResponse sendFax(@RequestParam("clientId") Integer clientId) {
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        return workflowService.sendFax(clientId);
    }
}
