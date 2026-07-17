package com.airbnbspa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyInfoDTO {

    private String name;
    private String description;
    private int maxGuests;
    private int bedrooms;
    private int beds;
    private int bathrooms;
    private List<String> amenities;
    private List<String> rules;
    private String checkInTime;
    private String checkOutTime;
}