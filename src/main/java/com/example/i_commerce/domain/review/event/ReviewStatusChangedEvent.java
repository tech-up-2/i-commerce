package com.example.i_commerce.domain.review.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReviewStatusChangedEvent {

    private final Long reporterId;
    private final String message;

}
