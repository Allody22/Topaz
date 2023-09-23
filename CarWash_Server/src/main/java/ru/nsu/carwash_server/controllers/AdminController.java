package ru.nsu.carwash_server.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.carwash_server.models.orders.OrderVersions;
import ru.nsu.carwash_server.models.secondary.constants.ERole;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.models.secondary.helpers.OrderPriceTimeDoneTypeInfo;
import ru.nsu.carwash_server.models.users.Role;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.models.users.UserVersions;
import ru.nsu.carwash_server.payload.request.UpdateUserInfoRequest;
import ru.nsu.carwash_server.payload.response.MessageResponse;
import ru.nsu.carwash_server.payload.response.UserInformationResponse;
import ru.nsu.carwash_server.payload.response.UserOrdersResponse;
import ru.nsu.carwash_server.repository.users.RoleRepository;
import ru.nsu.carwash_server.services.OperationsService;
import ru.nsu.carwash_server.services.OrderServiceImp;
import ru.nsu.carwash_server.services.UserDetailsImpl;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final RoleRepository roleRepository;

    private final OrderServiceImp orderServiceImp;

    private final OperationsService operationsService;

    private final UserService userService;

    @Autowired
    public AdminController(
            OrderServiceImp orderServiceImp,
            OperationsService operationsService,
            RoleRepository roleRepository,
            UserService userService) {
        this.operationsService = operationsService;
        this.orderServiceImp = orderServiceImp;
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/adminRoleCheck")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> adminAccess() {
        return ResponseEntity.ok(new MessageResponse("Доступ есть"));
    }

    @GetMapping("/findUserByTelephone_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> findUserByTelephone(@Valid @RequestParam("username") String username) {

        UserVersions latestUserVersion = userService.getActualUserVersionByUsername(username);
        User user = latestUserVersion.getUser();

        return ResponseEntity.ok(new UserInformationResponse(user.getOrders(), user.getId(),
                latestUserVersion.getFullName(), latestUserVersion.getPhone(), latestUserVersion.getEmail(),
                latestUserVersion.getBonuses(), user.getRoles(), latestUserVersion.getComments(), latestUserVersion.getAdminNote()));
    }

    @GetMapping("/getUserOrdersByAdmin_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> getUserOrdersByAdmin(@Valid @RequestParam("username") String username) {

        User user = userService.getActualUserVersionByUsername(username).getUser();

        List<OrderPriceTimeDoneTypeInfo> userOrders = new ArrayList<>();

        for (var item : user.getOrders()) {
            OrderVersions latestOrderVersion = orderServiceImp.getActualOrderVersion(item.getId());

            userOrders.add(new OrderPriceTimeDoneTypeInfo(latestOrderVersion.getOrderType(),
                    latestOrderVersion.getPrice(), item.getId(),
                    latestOrderVersion.getStartTime(), latestOrderVersion.getCurrentStatus()));
        }

        return ResponseEntity.ok(new UserOrdersResponse(userOrders));
    }

    @GetMapping("/getAllUserNames_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> getAllUser() {
        return ResponseEntity.ok(userService.getAllActualUsernames());
    }

    @PostMapping("/updateUserInfo_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> updateUserInfo(@Valid @RequestBody UpdateUserInfoRequest updateUserInfoRequest) {
        var latestUserVersion = userService.getActualUserVersionByUsername(updateUserInfoRequest.getUsername());

        User user = latestUserVersion.getUser();
        Long userId = user.getId();
        Set<Role> roles;
        Set<String> strRoles = updateUserInfoRequest.getRoles();

        if (strRoles == null) {
            roles = latestUserVersion.getUser().getRoles();
        } else {
            Set<ERole> rolesList = EnumSet.allOf(ERole.class);
            roles = strRoles.stream().map(role -> {
                Optional<ERole> enumRole = rolesList.stream()
                        .filter(r -> r.name().equalsIgnoreCase(role))
                        .findAny();
                if (enumRole.isPresent()) {
                    return roleRepository.findByName(enumRole.get())
                            .orElseThrow(() -> new NotInDataBaseException("ролей не найдена роль: ", enumRole.get().name()));
                } else {
                    throw new RuntimeException("Ошибка: Неправильная роль:" + strRoles);
                }
            }).collect(Collectors.toSet());
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new NotInDataBaseException("ролей не найдена роль: ", ERole.ROLE_USER.name()));
            roles.add(userRole);
        }
        user.setRoles(roles);

        UserVersions newVersion = new UserVersions(latestUserVersion, updateUserInfoRequest);
        user.addUserVersion(newVersion);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userAdminId = userDetails.getId();
        UserVersions userAdmin = userService.getActualUserVersionById(userAdminId);

        String operationName = "Update_user_info_by_admin";
        String descriptionMessage = getString(updateUserInfoRequest, newVersion);

        operationsService.SaveUserOperation(operationName, userAdmin.getUser(), descriptionMessage, 1);

        return ResponseEntity.ok(new MessageResponse(descriptionMessage));
    }

    private static String getString(UpdateUserInfoRequest updateUserInfoRequest, UserVersions newVersion) {
        String newUsername = (updateUserInfoRequest.getUsername() != null) ?
                "новый username: '" + updateUserInfoRequest.getUsername() + "'," : null;

        String newFullName = (updateUserInfoRequest.getFullName() != null) ?
                "новое ФИО: '" + updateUserInfoRequest.getFullName() + "'," : null;

        String newRoles = (updateUserInfoRequest.getRoles() != null) ?
                "новый набор ролей: " + updateUserInfoRequest.getRoles() + "'," : null;

        String newAdminNote = (updateUserInfoRequest.getAdminNote() != null) ?
                "новую заметку от администратора: '" + updateUserInfoRequest.getAdminNote() + "'," : null;

        String newEmail = (updateUserInfoRequest.getEmail() != null) ?
                "новую почту: '" + updateUserInfoRequest.getEmail() + "'," : null;

        String newUserNote = (updateUserInfoRequest.getUserNote() != null) ?
                "новую заметку от самого пользователя: '" + updateUserInfoRequest.getUserNote() + "'," : null;

        return "Пользователь '" + newVersion.getUsername() + "' получил" + newUsername +
                newFullName + newRoles + newAdminNote + newEmail + newUserNote;
    }
}
