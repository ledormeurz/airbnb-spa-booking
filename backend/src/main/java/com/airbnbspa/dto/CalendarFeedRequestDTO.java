package com.airbnbspa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarFeedRequestDTO {

    @NotBlank(message = "Le nom est requis")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "L'URL iCal est requise")
    @Size(max = 1000)
    private String url;

    @Size(max = 50)
    private String source;

    private Boolean enabled;
}
