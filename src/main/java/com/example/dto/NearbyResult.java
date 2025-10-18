package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyResult {
    private List<PlaceCandidate> pharmacies;
    private List<PlaceCandidate> labs;
}

