package com.airbnbspa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarSyncResultDTO {

    private Long feedId;
    private String feedName;
    private String source;
    private String status;
    private String message;
    private IcalImportResultDTO importResult;
}
