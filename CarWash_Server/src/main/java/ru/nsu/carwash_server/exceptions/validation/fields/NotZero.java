package ru.nsu.carwash_server.exceptions.validation.fields;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = NotZeroValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotZero {
    String message() default "{NotZero.orderTime}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
