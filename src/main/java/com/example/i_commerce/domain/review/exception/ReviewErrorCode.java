package com.example.i_commerce.domain.review.exception;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {
    ALREADY_REVIEWED(HttpStatus.CONFLICT, "REV-40901", "이미 리뷰를 작성한 상품입니다."),
    ALREADY_COMMENTED(HttpStatus.CONFLICT, "REV-40902", "이미 답글을 작성한 상품입니다."),
    INVALID_STAR_RATING(HttpStatus.BAD_REQUEST, "REV-40903", "별점은 1~5점 사이여야 합니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REV-40904", "해당 리뷰를 찾을 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
