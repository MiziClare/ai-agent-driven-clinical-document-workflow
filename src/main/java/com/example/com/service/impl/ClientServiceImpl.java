package com.example.com.service.impl;

import com.example.com.entity.po.Client;
import com.example.com.mapper.ClientMapper;
import com.example.com.service.IClientService;
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
