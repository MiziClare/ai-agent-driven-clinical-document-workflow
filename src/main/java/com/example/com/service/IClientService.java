package com.example.com.service;

import com.example.com.entity.po.Client;
import java.util.List;

public interface IClientService {
    Client getClientById(Integer clientId);
    List<Client> getAllClients();
    int addClient(Client client);
    int updateClient(Client client);
    int deleteClient(Integer clientId);
}
