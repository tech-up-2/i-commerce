package com.example.i_commerce.global.validation.annotations;

import com.example.i_commerce.global.validation.validators.NoDuplicatesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoDuplicatesValidator.class)
public @interface NoDuplicates {
    String message() default "중복된 값이 존재합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}