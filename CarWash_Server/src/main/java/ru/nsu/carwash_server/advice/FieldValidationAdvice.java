package ru.nsu.carwash_server.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class FieldValidationAdvice {

    private MessageSource messageSource;

    @Autowired
    FieldValidationAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex, Locale locale) {
        log.error("FIELD VALIDATION ADVICE: " + ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = messageSource.getMessage(error, locale);
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleTypeMismatch(MethodArgumentTypeMismatchException ex, Locale locale) {
        log.error("METHOD ARGUMENT TYPE ADVICE: " + ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        String errorKey = "Invalid." + ex.getName();  // Например: Invalid.startTime
        String defaultMessage = "Неверное значение параметра: " + ex.getName(); // или вы можете определить более специфическое сообщение
        String errorMessage = messageSource.getMessage(errorKey, null, defaultMessage, locale);
        errors.put(ex.getName(), errorMessage);
        return errors;
    }
}
