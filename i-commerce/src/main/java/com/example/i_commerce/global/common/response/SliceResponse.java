package com.example.i_commerce.global.common.response;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Slice;

public record SliceResponse<T>(
    List<T> content,
    int sliceNumber,
    int numberOfElements,
    int size,
    boolean hasNext,
    boolean isFirst,
    boolean isLast
) {
    public static <T> SliceResponse<T> of(Slice<T> slice) {
        return new SliceResponse<>(
            slice.getContent(),
            slice.getNumber(),
            slice.getNumberOfElements(),
            slice.getSize(),
            slice.hasNext(),
            slice.isFirst(),
            slice.isLast()
        );
    }

    public static <T, R> SliceResponse<R> of(Slice<T> slice, Function<T, R> converter) {
        return new SliceResponse<>(
            slice.getContent().stream().map(converter).toList(),
            slice.getNumber(),
            slice.getNumberOfElements(),
            slice.getSize(),
            slice.hasNext(),
            slice.isFirst(),
            slice.isLast()
        );
    }
}
