package ru.nsu.carwash_server.controllers;


import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.carwash_server.models.operations.OperationsUserLink;
import ru.nsu.carwash_server.payload.response.OperationsResponse;
import ru.nsu.carwash_server.services.interfaces.OperationService;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Hidden
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/operations")
public class OperationController {

    private final OperationService operationsService;

    private final UserService userService;

    @Autowired
    public OperationController(
            OperationService operationsService,
            UserService userService) {
        this.userService = userService;
        this.operationsService = operationsService;

    }

    @GetMapping("/names")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<List<String>> getOperationsNames() {
        List<String> operationsNames = operationsService.getAllOperationsNames();

        return ResponseEntity.ok(operationsNames);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<List<OperationsResponse>> getUserOperations(@Valid @NotBlank @RequestParam("phone") String phone) {
        List<OperationsUserLink> userOperations = operationsService.getAllUserOperationsByIdOrPhone(null, phone);

        List<OperationsResponse> allOperationsInThisTime = new ArrayList<>();
        for (OperationsUserLink thisOperation : userOperations) {
            OperationsResponse currentOperation = new OperationsResponse();
            currentOperation.setId(thisOperation.getId());
            currentOperation.setDescription(thisOperation.getDescription());
            currentOperation.setDateOfCreation(thisOperation.getDateOfCreation());
            if (thisOperation.getUser() != null) {
                currentOperation.setUsername(userService.getActualUserVersionById(thisOperation.getUser().getId()).getPhone());
            } else {
                currentOperation.setUsername("Не зарегистрирован");
            }
            allOperationsInThisTime.add(currentOperation);
        }

        return ResponseEntity.ok(allOperationsInThisTime);
    }

    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @GetMapping("/operation_name")
    @Transactional
    public ResponseEntity<List<OperationsResponse>> getOperationsByName(@Valid @NotBlank @RequestParam("operation") String operation) {
        List<OperationsUserLink> allOperationsInATime = operationsService.getAllOperationsByName(operation);

        List<OperationsResponse> allOperationsByName = new ArrayList<>();
        for (OperationsUserLink thisOperation : allOperationsInATime) {
            OperationsResponse currentOperation = new OperationsResponse();
            currentOperation.setId(thisOperation.getId());
            currentOperation.setDescription(thisOperation.getDescription());
            currentOperation.setDateOfCreation(thisOperation.getDateOfCreation());
            if (thisOperation.getUser() != null) {
                currentOperation.setUsername(userService.getActualUserVersionById(thisOperation.getUser().getId()).getPhone());
            } else {
                currentOperation.setUsername("Не зарегистрирован");
            }
            allOperationsByName.add(currentOperation);
        }

        return ResponseEntity.ok(allOperationsByName);
    }

    @GetMapping("/get_all_day")
    @Transactional
    public ResponseEntity<List<OperationsResponse>> getAllOperationsInDay(@Valid @NotNull @RequestParam(name = "startTime")
                                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
                                                                          @Valid @NotNull @RequestParam(name = "endTime")
                                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime) {
        List<OperationsUserLink> allOperationsInATime = operationsService.getAllOperationsInATime(startTime, endTime);

        List<OperationsResponse> allOperationsInThisTime = new ArrayList<>();
        for (OperationsUserLink thisOperation : allOperationsInATime) {
            OperationsResponse currentOperation = new OperationsResponse();
            currentOperation.setId(thisOperation.getId());
            currentOperation.setDescription(thisOperation.getDescription());
            currentOperation.setDateOfCreation(thisOperation.getDateOfCreation());
            if (thisOperation.getUser() != null) {
                currentOperation.setUsername(userService.getActualUserVersionById(thisOperation.getUser().getId()).getPhone());
            } else {
                currentOperation.setUsername("Не зарегистрирован");
            }
            allOperationsInThisTime.add(currentOperation);
        }

        return ResponseEntity.ok(allOperationsInThisTime);
    }


    @GetMapping("/get_all")
    @Transactional
    public ResponseEntity<List<OperationsResponse>> getAllOperations() {

        List<OperationsUserLink> allOperationsInATime = operationsService.getAllOperations();

        List<OperationsResponse> allOperationsInThisTime = new ArrayList<>();
        for (OperationsUserLink thisOperation : allOperationsInATime) {
            OperationsResponse currentOperation = new OperationsResponse();
            currentOperation.setId(thisOperation.getId());
            currentOperation.setDescription(thisOperation.getDescription());
            currentOperation.setDateOfCreation(thisOperation.getDateOfCreation());
            if (thisOperation.getUser() != null) {
                currentOperation.setUsername(userService.getActualUserVersionById(thisOperation.getUser().getId()).getPhone());
            } else {
                currentOperation.setUsername("Не зарегистрирован");
            }
            allOperationsInThisTime.add(currentOperation);
        }

        Collections.reverse(allOperationsInThisTime);

        return ResponseEntity.ok(allOperationsInThisTime);
    }
}
