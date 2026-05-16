package com.example.i_commerce.global.validation.validators;

import com.example.i_commerce.global.validation.annotations.NoDuplicates;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoDuplicatesValidator implements ConstraintValidator<NoDuplicates, List<?>> {

    @Override
    public boolean isValid(List<?> values, ConstraintValidatorContext context) {
        if (values == null || values.isEmpty()) {
            return true;
        }
        Set<?> uniqueValues = new HashSet<>(values);
        return uniqueValues.size() == values.size();
    }

}
