package com.example.service.impl;

import com.example.entity.po.Client;
import com.example.mapper.ClientMapper;
import com.example.service.IClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientServiceImpl implements IClientService {

    @Autowired
    private ClientMapper clientMapper;

    @Override
    public Client getClientById(Integer clientId) {
        return clientMapper.selectById(clientId);
    }

    @Override
    public List<Client> getAllClients() {
        return clientMapper.selectAll();
    }

    @Override
    public int addClient(Client client) {
        return clientMapper.insert(client);
    }

    @Override
    public int updateClient(Client client) {
        return clientMapper.update(client);
    }

    @Override
    public int deleteClient(Integer clientId) {
        return clientMapper.delete(clientId);
    }
}
