package ru.nsu.carwash_server.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.carwash_server.models.OperationsVersions;
import ru.nsu.carwash_server.services.OperationsService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/operations")
public class OperationController {

    private final OperationsService operationsService;

    @Autowired
    public OperationController(
            OperationsService operationsService) {
        this.operationsService = operationsService;

    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> getUserOperations(@Valid @RequestParam("username") String username) {
        List<OperationsVersions> userOperations = operationsService.getAllUserOperationsByIdOrUsername(null, username);
        return ResponseEntity.ok(operationsService.getRationalOperationForm(userOperations));
    }
}
