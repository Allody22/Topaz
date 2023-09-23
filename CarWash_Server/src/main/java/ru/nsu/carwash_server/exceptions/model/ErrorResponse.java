package ru.nsu.carwash_server.exceptions.model;

import lombok.Builder;
import lombok.Data;
import ru.nsu.carwash_server.exceptions.validation_violation.FieldValidationViolation;
import ru.nsu.carwash_server.exceptions.validation_violation.HttpAttributeValidationViolation;
import ru.nsu.carwash_server.exceptions.validation_violation.ObjectValidationViolation;

import java.util.List;

@Data
@Builder
public class ErrorResponse {
    private String error;
    private List<ObjectValidationViolation> objectValidationViolations;
    private List<FieldValidationViolation> fieldValidationViolations;
    private List<HttpAttributeValidationViolation> httpAttributeValidationViolations;
}
