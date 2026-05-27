package com.example.i_commerce.domain.review.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StarRateCountProjection {

    Integer getStarRate();
    Long getCount();


}
