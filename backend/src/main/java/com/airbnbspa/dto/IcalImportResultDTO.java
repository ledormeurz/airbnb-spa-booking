package com.airbnbspa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IcalImportResultDTO {

    private int totalEvents;
    private int imported;
    private int updated;
    private int skipped;
    private String source;
}
