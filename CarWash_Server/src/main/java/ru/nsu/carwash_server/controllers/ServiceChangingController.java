package ru.nsu.carwash_server.controllers;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.carwash_server.exceptions.InvalidOrderTypeException;
import ru.nsu.carwash_server.models.orders.OrdersWashing;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.payload.request.NewServiceRequest;
import ru.nsu.carwash_server.payload.request.UpdatePolishingServiceRequest;
import ru.nsu.carwash_server.payload.request.UpdateTireServiceRequest;
import ru.nsu.carwash_server.payload.request.UpdateWashingServiceRequest;
import ru.nsu.carwash_server.payload.response.MessageResponse;
import ru.nsu.carwash_server.repository.orders.OrdersPolishingRepository;
import ru.nsu.carwash_server.repository.orders.OrdersTireRepository;
import ru.nsu.carwash_server.repository.orders.OrdersWashingRepository;
import ru.nsu.carwash_server.services.UserDetailsImpl;
import ru.nsu.carwash_server.services.interfaces.OperationService;
import ru.nsu.carwash_server.services.interfaces.OrderService;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/admin/services")
@AllArgsConstructor
public class ServiceChangingController {
    private final OrderService orderService;

    private final OrdersWashingRepository ordersWashingRepository;

    private final OrdersPolishingRepository polishingRepository;

    private final OrdersTireRepository tireRepository;

    private final UserService userService;

    private final OperationService operationsService;

    @Autowired
    public ServiceChangingController(
            UserService userService,
            OperationService operationsService,
            OrdersWashingRepository ordersWashingRepository,
            OrdersPolishingRepository polishingRepository,
            OrdersTireRepository tireRepository,
            OrderService orderService
    ) {
        this.polishingRepository = polishingRepository;
        this.userService = userService;
        this.operationsService = operationsService;
        this.ordersWashingRepository = ordersWashingRepository;
        this.tireRepository = tireRepository;
        this.orderService = orderService;
    }


    @PostMapping("/createNewService_v1")
    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> createNewService(@Valid @RequestBody NewServiceRequest newServiceRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        String operationName = "Create_new_service_by_admin";
        User user = userService.getFullUserById(userId);

        String serviceType = newServiceRequest.getServiceType();
        String userPhone = userService.getActualUserVersionById(userId).getPhone();
        switch (serviceType) {
            case "wash" -> {
                if (newServiceRequest.getRole() == null || newServiceRequest.getRole().isBlank()) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Пожалуйста, введи роль для услуги мойки"));
                }

                Pair<String, OrdersWashing> resultOfSaving = orderService.createWashingService(newServiceRequest);

                operationsService.SaveUserOperation(operationName, user, resultOfSaving.getFirst(), 1);
                log.info("createNewService_v1. User with phone '{}' created new washing service.", userPhone);

                return (ResponseEntity.ok(resultOfSaving.getSecond()));
            }
            case "tire" -> {

                var resultOfSaving = orderService.createTireService(newServiceRequest);

                operationsService.SaveUserOperation(operationName, user, resultOfSaving.getFirst(), 1);
                log.info("createNewService_v1. User with phone '{}' created new tire service.", userPhone);

                return (ResponseEntity.ok(resultOfSaving.getSecond()));

            }
            case "polishing" -> {

                var resultOfSaving = orderService.createPolishingService(newServiceRequest);
                operationsService.SaveUserOperation(operationName, user, resultOfSaving.getFirst(), 1);

                log.info("createNewService_v1. User with phone '{}' created new polishing service.", userPhone);

                return (ResponseEntity.ok(resultOfSaving.getSecond()));
            }
            default -> throw new InvalidOrderTypeException(serviceType);
        }
    }

    @PutMapping("/updateWashingService_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<MessageResponse> updateWashingService(@Valid @RequestBody UpdateWashingServiceRequest updateWashingServiceRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        User user = userService.getFullUserById(userId);
        String userPhone = userService.getActualUserVersionById(userId).getPhone();
        String operationName = "Update_washing_service_by_admin";

        String descriptionMessage = orderService.updateWashingService(updateWashingServiceRequest);

        log.info("updateWashingService_v1. User with phone '{}' updated washing service.", userPhone);

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        return ResponseEntity.ok(new MessageResponse(descriptionMessage));
    }

    @Transactional
    @PutMapping("/updatePolishingService_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<MessageResponse> createPolishingService(@Valid @RequestBody UpdatePolishingServiceRequest updatePolishingServiceRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        User user = userService.getFullUserById(userId);

        String userPhone = userService.getActualUserVersionById(userId).getPhone();

        String operationName = "Update_polishing_service_by_admin";

        String descriptionMessage = orderService.updatePolishingService(updatePolishingServiceRequest);
        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);
        log.info("updatePolishingService_v1. User with phone '{}' updated polishing service.", userPhone);

        return ResponseEntity.ok(new MessageResponse("Услуга " + updatePolishingServiceRequest.getName().replace("_", " ") + " изменена"));
    }

    @PutMapping("/updateTireService_v1")
    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<MessageResponse> updateTireService(@Valid @RequestBody UpdateTireServiceRequest updateTireServiceRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        String operationName = "Update_tire_service_by_admin";

        User user = userService.getFullUserById(userId);
        String userPhone = userService.getActualUserVersionById(userId).getPhone();

        String descriptionMessage = orderService.updateTireService(updateTireServiceRequest);

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);
        log.info("updatePolishingService_v1. User with phone '{}' updated tire service.", userPhone);

        return ResponseEntity.ok(new MessageResponse("Услуга " + updateTireServiceRequest.getName().replace("_", " ") + " изменена"));
    }
}
