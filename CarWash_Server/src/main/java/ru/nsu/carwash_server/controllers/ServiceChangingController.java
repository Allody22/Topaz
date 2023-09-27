package ru.nsu.carwash_server.controllers;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.carwash_server.models.orders.OrdersPolishing;
import ru.nsu.carwash_server.models.orders.OrdersTire;
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
import ru.nsu.carwash_server.services.OperationsServiceIml;
import ru.nsu.carwash_server.services.UserDetailsImpl;
import ru.nsu.carwash_server.services.interfaces.OrderService;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.StringJoiner;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/services")
@AllArgsConstructor
public class ServiceChangingController {
    private final OrderService orderService;

    private final OrdersWashingRepository ordersWashingRepository;

    private final OrdersPolishingRepository polishingRepository;

    private final OrdersTireRepository tireRepository;

    private final UserService userService;
    
    private final OperationsServiceIml operationsService;

    @Autowired
    public ServiceChangingController(
            UserService userService,
            OperationsServiceIml operationsService,
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
        
        switch (newServiceRequest.getServiceType()) {
            case "wash" -> {
                StringJoiner joiner = new StringJoiner(";");
                for (String element : newServiceRequest.getIncludedIn()) {
                    joiner.add(element);
                }

                String serviceRoleInApp = joiner.toString();
                OrdersWashing ordersWashing = OrdersWashing.builder().
                        name(newServiceRequest.getName())
                        .priceFirstType(newServiceRequest.getPriceFirstType())
                        .priceSecondType(newServiceRequest.getPriceSecondType())
                        .priceThirdType(newServiceRequest.getPriceThirdType())
                        .timeFirstType(newServiceRequest.getTimeFirstType())
                        .timeSecondType(newServiceRequest.getTimeSecondType())
                        .timeThirdType(newServiceRequest.getTimeThirdType())
                        .associatedOrder(serviceRoleInApp)
                        .role(newServiceRequest.getRole())
                        .build();
                var response = orderService.createWashingService(ordersWashing);
                String newPriceFirstType = (newServiceRequest.getPriceFirstType() != null) ?
                        " цену за 1 тип: '" + newServiceRequest.getPriceThirdType() + "', " : null;

                String newPriceSecondType = (newServiceRequest.getPriceSecondType() != null) ?
                        " цену за 2 тип: '" + newServiceRequest.getPriceSecondType() + "', " : null;

                String newPriceThirdType = (newServiceRequest.getPriceThirdType() != null) ?
                        " цену за 3 тип: '" + newServiceRequest.getTimeThirdType() + "', " : null;

                String newTimeFirstType = (newServiceRequest.getTimeFirstType() != null) ?
                        " время за 1 тип: '" + newServiceRequest.getTimeFirstType() + "', " : null;

                String newTimeSecondType = (newServiceRequest.getTimeSecondType() != null) ?
                        " время за 2 тип: '" + newServiceRequest.getTimeSecondType() + "', " : null;

                String newTimeThirdType = (newServiceRequest.getTimeThirdType() != null) ?
                        " время за 3 тип: '" + newServiceRequest.getTimeThirdType() + "', " : null;


                String descriptionMessage = "Создана услуга мойки '" + newServiceRequest.getName().replace("_", " ")
                        + "', получившая " + newPriceFirstType + newPriceSecondType + newPriceThirdType +
                        newTimeFirstType + newTimeSecondType + newTimeThirdType;

                operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

                return (ResponseEntity.ok(response));
            }
            case "tire" -> {
                OrdersTire ordersTire = OrdersTire.builder()
                        .name(newServiceRequest.getName())
                        .price_r_13(newServiceRequest.getPrice_r_13())
                        .price_r_14(newServiceRequest.getPrice_r_14())
                        .price_r_15(newServiceRequest.getPrice_r_15())
                        .price_r_16(newServiceRequest.getPrice_r_16())
                        .price_r_17(newServiceRequest.getPrice_r_17())
                        .price_r_18(newServiceRequest.getPrice_r_18())
                        .price_r_19(newServiceRequest.getPrice_r_19())
                        .price_r_20(newServiceRequest.getPrice_r_20())
                        .price_r_21(newServiceRequest.getPrice_r_21())
                        .price_r_22(newServiceRequest.getPrice_r_22())
                        .time_r_13(newServiceRequest.getTime_r_13())
                        .time_r_14(newServiceRequest.getTime_r_14())
                        .time_r_15(newServiceRequest.getTime_r_15())
                        .time_r_16(newServiceRequest.getTime_r_16())
                        .time_r_17(newServiceRequest.getTime_r_17())
                        .time_r_18(newServiceRequest.getTime_r_18())
                        .time_r_19(newServiceRequest.getTime_r_19())
                        .time_r_20(newServiceRequest.getTime_r_20())
                        .time_r_21(newServiceRequest.getTime_r_21())
                        .time_r_22(newServiceRequest.getTime_r_22())
                        .role(newServiceRequest.getRole())
                        .build();
                var response = orderService.createTireService(ordersTire);
                String descriptionMessage = "Создана услуга шиномонтажа'" + newServiceRequest.getName().replace("_", " ");

                operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

                return (ResponseEntity.ok(response));

            }
            case "polishing" -> {
                OrdersPolishing ordersPolishing = OrdersPolishing.builder().
                        name(newServiceRequest.getName())
                        .priceFirstType(newServiceRequest.getPriceFirstType())
                        .priceSecondType(newServiceRequest.getPriceSecondType())
                        .priceThirdType(newServiceRequest.getPriceThirdType())
                        .timeFirstType(newServiceRequest.getTimeFirstType())
                        .timeSecondType(newServiceRequest.getTimeSecondType())
                        .timeThirdType(newServiceRequest.getTimeThirdType())
                        .build();
                var response = orderService.createPolishingService(ordersPolishing);

                String newPriceFirstType = (newServiceRequest.getPriceFirstType() != null) ?
                        " цену за 1 тип: '" + newServiceRequest.getPriceThirdType() + "', " : null;

                String newPriceSecondType = (newServiceRequest.getPriceSecondType() != null) ?
                        " цену за 2 тип: '" + newServiceRequest.getPriceSecondType() + "', " : null;

                String newPriceThirdType = (newServiceRequest.getPriceThirdType() != null) ?
                        " цену за 3 тип: '" + newServiceRequest.getTimeThirdType() + "', " : null;

                String newTimeFirstType = (newServiceRequest.getTimeFirstType() != null) ?
                        " время за 1 тип: '" + newServiceRequest.getTimeFirstType() + "', " : null;

                String newTimeSecondType = (newServiceRequest.getTimeSecondType() != null) ?
                        " время за 2 тип: '" + newServiceRequest.getTimeSecondType() + "', " : null;

                String newTimeThirdType = (newServiceRequest.getTimeThirdType() != null) ?
                        " время за 3 тип: '" + newServiceRequest.getTimeThirdType() + "', " : null;


                String descriptionMessage = "Создана услуга полировка'" + newServiceRequest.getName().replace("_", " ")
                        + "', получившая " + newPriceFirstType + newPriceSecondType + newPriceThirdType +
                        newTimeFirstType + newTimeSecondType + newTimeThirdType;
                operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

                return (ResponseEntity.ok(response));
            }
        }

        return ResponseEntity.badRequest().body(new MessageResponse("Типа услуг "
                + newServiceRequest.getServiceType() + " не существует"));
    }

    @PutMapping("/updateWashingService_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> updateWashingService(@Valid @RequestBody UpdateWashingServiceRequest updateWashingServiceRequest) {
        if (ordersWashingRepository.findByName(updateWashingServiceRequest.getName()).isPresent()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = userDetails.getId();
            ordersWashingRepository.updateWashingServiceInfo(updateWashingServiceRequest.getName(), updateWashingServiceRequest.getPriceFirstType(),
                    updateWashingServiceRequest.getPriceSecondType(), updateWashingServiceRequest.getPriceThirdType(),
                    updateWashingServiceRequest.getTimeFirstType(), updateWashingServiceRequest.getTimeSecondType(),
                    updateWashingServiceRequest.getTimeThirdType(), updateWashingServiceRequest.getRole());


            User user = userService.getFullUserById(userId);

            String operationName = "Update_washing_service_by_admin";

            String newPriceFirstType = (updateWashingServiceRequest.getPriceFirstType() != null) ?
                    "новую цену за 1 тип: '" + updateWashingServiceRequest.getPriceThirdType() + "', " : null;

            String newPriceSecondType = (updateWashingServiceRequest.getPriceSecondType() != null) ?
                    "новую цену за 2 тип: '" + updateWashingServiceRequest.getPriceSecondType() + "', " : null;

            String newPriceThirdType = (updateWashingServiceRequest.getPriceThirdType() != null) ?
                    "новую цену за 3 тип: '" + updateWashingServiceRequest.getTimeThirdType() + "', " : null;

            String newTimeFirstType = (updateWashingServiceRequest.getTimeFirstType() != null) ?
                    "новое время за 1 тип: '" + updateWashingServiceRequest.getTimeFirstType() + "', " : null;

            String newTimeSecondType = (updateWashingServiceRequest.getTimeSecondType() != null) ?
                    "новое время за 2 тип: '" + updateWashingServiceRequest.getTimeSecondType() + "', " : null;

            String newTimeThirdType = (updateWashingServiceRequest.getTimeThirdType() != null) ?
                    "новое время за 3 тип: '" + updateWashingServiceRequest.getTimeThirdType() + "', " : null;


            String descriptionMessage = "Услуга '" + updateWashingServiceRequest.getName().replace("_", " ")
                    + "' получила " + newPriceFirstType + newPriceSecondType + newPriceThirdType +
                    newTimeFirstType + newTimeSecondType + newTimeThirdType;

            operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

            return ResponseEntity.ok(new MessageResponse(descriptionMessage));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуги "
                    + updateWashingServiceRequest.getName().replace("_", " ") + " не существует"));
        }
    }

    @Transactional
    @PutMapping("/updatePolishingService_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> createPolishingService(@Valid @RequestBody UpdatePolishingServiceRequest updatePolishingServiceRequest) {
        if (polishingRepository.findByName(updatePolishingServiceRequest.getName()).isPresent()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = userDetails.getId();

            polishingRepository.updatePolishingOrder(updatePolishingServiceRequest.getName(), updatePolishingServiceRequest.getPriceFirstType(),
                    updatePolishingServiceRequest.getPriceSecondType(), updatePolishingServiceRequest.getPriceThirdType(),
                    updatePolishingServiceRequest.getTimeFirstType(), updatePolishingServiceRequest.getTimeSecondType(),
                    updatePolishingServiceRequest.getTimeThirdType());

            User user = userService.getFullUserById(userId);

            String operationName = "Update_polishing_service_by_admin";

            String newPriceFirstType = (updatePolishingServiceRequest.getPriceFirstType() != null) ?
                    "новую цену за 1 тип: '" + updatePolishingServiceRequest.getPriceThirdType() + "', " : null;

            String newPriceSecondType = (updatePolishingServiceRequest.getPriceSecondType() != null) ?
                    "новую цену за 2 тип: '" + updatePolishingServiceRequest.getPriceSecondType() + "', " : null;

            String newPriceThirdType = (updatePolishingServiceRequest.getPriceThirdType() != null) ?
                    "новую цену за 3 тип: '" + updatePolishingServiceRequest.getTimeThirdType() + "', " : null;

            String newTimeFirstType = (updatePolishingServiceRequest.getTimeFirstType() != null) ?
                    "новое время за 1 тип: '" + updatePolishingServiceRequest.getTimeFirstType() + "', " : null;

            String newTimeSecondType = (updatePolishingServiceRequest.getTimeSecondType() != null) ?
                    "новое время за 2 тип: '" + updatePolishingServiceRequest.getTimeSecondType() + "', " : null;

            String newTimeThirdType = (updatePolishingServiceRequest.getTimeThirdType() != null) ?
                    "новое время за 3 тип: '" + updatePolishingServiceRequest.getTimeThirdType() + "', " : null;


            String descriptionMessage = "Услуга '" + updatePolishingServiceRequest.getName().replace("_", " ")
                    + "' получила " + newPriceFirstType + newPriceSecondType + newPriceThirdType +
                    newTimeFirstType + newTimeSecondType + newTimeThirdType;

            operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

            return ResponseEntity.ok(new MessageResponse("Услуга " + updatePolishingServiceRequest.getName().replace("_", " ") + " изменена"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуги "
                    + updatePolishingServiceRequest.getName().replace("_", " ") + " не существует"));
        }
    }

    @PutMapping("/updateTireService_v1")
    @Transactional
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> updateTireService(@Valid @RequestBody UpdateTireServiceRequest updateTireServiceRequest) {
        if (tireRepository.findByName(updateTireServiceRequest.getName()).isPresent()) {
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

            String descriptionMessage = "Услуга '" + updateTireServiceRequest.getName().replace("_", " ")
                    + "' изменена";

            operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

            return ResponseEntity.ok(new MessageResponse("Услуга " + updateTireServiceRequest.getName().replace("_", " ") + " изменена"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Услуги "
                    + updateTireServiceRequest.getName().replace("_", " ") + " не существует"));
        }
    }
}
