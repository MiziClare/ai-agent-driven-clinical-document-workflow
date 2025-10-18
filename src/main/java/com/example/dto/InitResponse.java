package com.example.dto;

import com.example.entity.po.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitResponse {
    private Client client;
}

