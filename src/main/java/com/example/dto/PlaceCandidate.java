package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCandidate {
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private Double rating;
}

