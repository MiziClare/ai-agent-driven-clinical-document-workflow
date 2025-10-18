package com.example.service;

import com.example.entity.po.Client;
import java.util.List;

public interface IClientService {
    Client getClientById(Integer clientId);
    List<Client> getAllClients();
    int addClient(Client client);
    int updateClient(Client client);
    int deleteClient(Integer clientId);
}
