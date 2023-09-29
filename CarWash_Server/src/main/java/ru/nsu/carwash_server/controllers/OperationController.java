package ru.nsu.carwash_server.controllers;


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
import ru.nsu.carwash_server.models.operations.OperationsVersions;
import ru.nsu.carwash_server.payload.response.OperationsResponse;
import ru.nsu.carwash_server.services.interfaces.OperationService;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @GetMapping("/user")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> getUserOperations(@Valid @RequestParam("username") String username) {
        List<OperationsVersions> userOperations = operationsService.getAllUserOperationsByIdOrPhone(null, username);
        return ResponseEntity.ok(operationsService.getRationalOperationForm(userOperations));
    }

    @GetMapping("/get_all_day")
    @Transactional
    public ResponseEntity<?> getAllOperationsInDay(@Valid @RequestParam(name = "startTime")
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
                                                   @Valid @RequestParam(name = "endTime")
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime) {

        List<OperationsUserLink> allOperationsInATime = operationsService.getAllOperationsInATime(startTime, endTime);

        List<OperationsResponse> allOperationsInThisTime = new ArrayList<>();
        for (OperationsUserLink thisOperation : allOperationsInATime) {
            OperationsResponse currentOperation = new OperationsResponse();
            currentOperation.setId(thisOperation.getId());
            currentOperation.setDescription(thisOperation.getDescription());
            currentOperation.setDateOfCreation(thisOperation.getDateOfCreation());
            currentOperation.setUsername(userService.getActualUserVersionById(thisOperation.getUser().getId()).getPhone());
            allOperationsInThisTime.add(currentOperation);
        }

        return ResponseEntity.ok(allOperationsInThisTime);
    }


    @GetMapping("/get_all")
    @Transactional
    public ResponseEntity<?> getAllOperations() {

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
                // Устанавливаем какое-либо значение по умолчанию или просто пропускаем
                currentOperation.setUsername("Не зарегистрирован");
            }
            allOperationsInThisTime.add(currentOperation);
        }

        return ResponseEntity.ok(allOperationsInThisTime);
    }
}
