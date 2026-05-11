package com.example.i_commerce.domain.review.service.dto;

import com.example.i_commerce.domain.review.entity.enums.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "CreateReportRequest", description = "리뷰 신고 요청")
@Getter
@AllArgsConstructor
@Builder
public class CreateReportRequest {

    private String reason;

    private ReportType reportType;
}
