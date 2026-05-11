package com.example.i_commerce.domain.review.entity.enums;

public enum ReportType {
    SPAM("스팸/광고"),
    INFO_EXPOSURE("개인정보 노출"),
    PLAGIARISM("저작권 도용/불법 사용"),
    INAPPROPRIATE("부적절한 콘텐츠"),
    OTHER("기타");

    private final String description;

    ReportType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
