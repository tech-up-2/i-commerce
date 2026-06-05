package com.example.i_commerce.domain.review.service.dto;

import com.example.i_commerce.domain.review.entity.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {
    private String reason;
    private ReportType reportType;
}
