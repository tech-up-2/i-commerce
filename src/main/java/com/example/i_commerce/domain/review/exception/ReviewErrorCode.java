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
    ALREADY_REPORT(HttpStatus.CONFLICT, "REV-40903", "이미 신고를 한 상품입니다."),
    ALREADY_PROCESSED(HttpStatus.CONFLICT, "REV-40911", "이미 처리가 완료된 신고 리뷰입니다."),
    ALREADY_ASSIGNED_ADMIN(HttpStatus.CONFLICT, "REV-40910", "이미 관리자가 배정되어있습니다."),
    INVALID_STAR_RATING(HttpStatus.BAD_REQUEST, "REV-40904", "별점은 1~5점 사이여야 합니다."),
    INVALID_SELF_REPORTING(HttpStatus.BAD_REQUEST, "REV-40905", "자신의 리뷰를 신고할 수 없습니다."),
    ADMIN_ID_REQUIRED(HttpStatus.BAD_REQUEST, "REV-40906", "관리자 ID는 필수입니다."),
    FORBIDDEN_WORD_INCLUDED(HttpStatus.BAD_REQUEST, "REV-40913", "금칙어가 포함되어 있습니다.") ,
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REV-40907", "해당 리뷰를 찾을 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REV-40908", "해당 신고를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "REV-40912", "해당 답글을 찾을 수 없습니다."),
    NOT_ACTUAL_BUYER(HttpStatus.FORBIDDEN, "REV-40914", "주문 확정이 된 고객만 리뷰를 쓸 수 있습니다."),
    NOT_AUTHORIZED_ADMIN(HttpStatus.UNAUTHORIZED, "REV-40909", "해당 작업을 수행할 권한이 있는 관리자가 아닙니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
