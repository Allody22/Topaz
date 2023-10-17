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
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
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
                if (newServiceRequest.getRole().isBlank()) {
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
            default -> {
                throw new InvalidOrderTypeException(serviceType);
            }
        }
    }

    @PutMapping("/updateWashingService_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<MessageResponse> updateWashingService(@Valid @RequestBody UpdateWashingServiceRequest updateWashingServiceRequest) {
        String serviceName = updateWashingServiceRequest.getName();
        ordersWashingRepository.findByName(updateWashingServiceRequest.getName())
                .orElseThrow(() -> new NotInDataBaseException("услуг мойки не найдена услуга ", serviceName.replace("_", " ")));
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        int priceFirstType = updateWashingServiceRequest.getPriceFirstType();
        int priceSecondType = updateWashingServiceRequest.getPriceSecondType();
        int priceThirdType = updateWashingServiceRequest.getPriceThirdType();
        int timeFirstType = updateWashingServiceRequest.getTimeFirstType();
        int timeSecondType = updateWashingServiceRequest.getTimeSecondType();
        int timeThirdType = updateWashingServiceRequest.getTimeThirdType();
        String serviceRole = updateWashingServiceRequest.getRole();

        ordersWashingRepository.updateWashingServiceInfo(serviceName, priceFirstType, priceSecondType, priceThirdType,
                timeFirstType, timeSecondType, timeThirdType, serviceRole);

        User user = userService.getFullUserById(userId);
        String userPhone = userService.getActualUserVersionById(userId).getPhone();
        String operationName = "Update_washing_service_by_admin";

        String descriptionMessage = orderService.getPolishingWashingOrderChangingInfo(priceFirstType,
                priceSecondType, priceThirdType, timeFirstType, timeSecondType, timeThirdType, "Обновлена услуга", serviceName);

        log.info("updateWashingService_v1. User with phone '{}' updated washing service.", userPhone);

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        return ResponseEntity.ok(new MessageResponse(descriptionMessage));
    }

    @Transactional
    @PutMapping("/updatePolishingService_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<MessageResponse> createPolishingService(@Valid @RequestBody UpdatePolishingServiceRequest updatePolishingServiceRequest) {
        String serviceName = updatePolishingServiceRequest.getName();
        polishingRepository.findByName(serviceName)
                .orElseThrow(() -> new NotInDataBaseException("услуг полировки не найдена услуга ", serviceName.replace("_", " ")));
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        int priceFirstType = updatePolishingServiceRequest.getPriceFirstType();
        int priceSecondType = updatePolishingServiceRequest.getPriceSecondType();
        int priceThirdType = updatePolishingServiceRequest.getPriceThirdType();
        int timeFirstType = updatePolishingServiceRequest.getTimeFirstType();
        int timeSecondType = updatePolishingServiceRequest.getTimeSecondType();
        int timeThirdType = updatePolishingServiceRequest.getTimeThirdType();

        polishingRepository.updatePolishingOrder(serviceName, priceFirstType, priceSecondType, priceThirdType,
                timeFirstType, timeSecondType, timeThirdType);

        User user = userService.getFullUserById(userId);

        String userPhone = userService.getActualUserVersionById(userId).getPhone();

        String operationName = "Update_polishing_service_by_admin";

        String descriptionMessage = orderService.getPolishingWashingOrderChangingInfo(timeFirstType,
                priceSecondType, priceThirdType, timeFirstType, timeSecondType, timeThirdType, "Обновлена услуга", serviceName);
        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);
        log.info("updatePolishingService_v1. User with phone '{}' updated polishing service.", userPhone);

        return ResponseEntity.ok(new MessageResponse("Услуга " + updatePolishingServiceRequest.getName().replace("_", " ") + " изменена"));
    }

    @PutMapping("/updateTireService_v1")
    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<MessageResponse> updateTireService(@Valid @RequestBody UpdateTireServiceRequest updateTireServiceRequest) {
        String serviceName = updateTireServiceRequest.getName();

        tireRepository.findByName(serviceName)
                .orElseThrow(() -> new NotInDataBaseException("услуг шиномонтажа не найдена услуга ", serviceName.replace("_", " ")));
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        tireRepository.updateTireOrderInfo(updateTireServiceRequest.getName(), updateTireServiceRequest.getPrice_r_13(),
                updateTireServiceRequest.getPrice_r_14(), updateTireServiceRequest.getPrice_r_15(),
                updateTireServiceRequest.getPrice_r_16(), updateTireServiceRequest.getPrice_r_17(),
                updateTireServiceRequest.getPrice_r_18(), updateTireServiceRequest.getPrice_r_19(), updateTireServiceRequest.getPrice_r_20(),
                updateTireServiceRequest.getPrice_r_21(), updateTireServiceRequest.getPrice_r_22(),
                updateTireServiceRequest.getTime_r_13(), updateTireServiceRequest.getTime_r_14(),
                updateTireServiceRequest.getTime_r_15(), updateTireServiceRequest.getTime_r_16(),
                updateTireServiceRequest.getTime_r_17(), updateTireServiceRequest.getTime_r_18(),
                updateTireServiceRequest.getTime_r_19(), updateTireServiceRequest.getTime_r_20(),
                updateTireServiceRequest.getTime_r_21(), updateTireServiceRequest.getTime_r_22(),
                updateTireServiceRequest.getRole());

        String operationName = "Update_tire_service_by_admin";

        User user = userService.getFullUserById(userId);
        String userPhone = userService.getActualUserVersionById(userId).getPhone();

        String descriptionMessage = "Услуга '" + updateTireServiceRequest.getName().replace("_", " ")
                + "' изменена";

        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);
        log.info("updatePolishingService_v1. User with phone '{}' updated tire service.", userPhone);

        return ResponseEntity.ok(new MessageResponse("Услуга " + updateTireServiceRequest.getName().replace("_", " ") + " изменена"));
    }
}
