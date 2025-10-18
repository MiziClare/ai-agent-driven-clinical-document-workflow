package com.example.controller;

import com.example.entity.po.Client;
import com.example.service.IClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private IClientService clientService;

    // GET /api/clients - Get all clients
    @GetMapping
    public List<Client> getAllClients() {
        return clientService.getAllClients();
    }

    // GET /api/clients/{id} - Get client by ID
    @GetMapping("/{id}")
    public Client getClientById(@PathVariable("id") Integer id) {
        Client client = clientService.getClientById(id);
        if (client == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Client not found"
            );
        }
        return client;
    }

    // Add new client
    @PostMapping
    public int addClient(@RequestBody Client client) {
        return clientService.addClient(client);
    }

    // Update existing client
    @PutMapping
    public int updateClient(@RequestBody Client client) {
        int result = clientService.updateClient(client);
        if (result == 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Client not found"
            );
        }
        return result;
    }

    // Delete client by ID
    @DeleteMapping("/{id}")
    public int deleteClient(@PathVariable("id") Integer id) {
        int result = clientService.deleteClient(id);
        if (result == 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Client not found"
            );
        }
        return result;
    }

}
