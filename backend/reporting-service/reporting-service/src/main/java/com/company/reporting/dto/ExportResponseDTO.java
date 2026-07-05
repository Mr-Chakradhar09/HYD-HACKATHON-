package com.company.reporting.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportResponseDTO {
    private String fileName;
    private String contentType;
    private String status;
    private String downloadUrl;
}
