package ru.nsu.carwash_server.exceptions.validation.fields;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotZeroValidator implements ConstraintValidator<NotZero, Integer> {

    @Override
    public void initialize(NotZero notZero) {
    }

    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext context) {
        if (integer == null) {
            return true;
        }
        return integer != 0;
    }
}

