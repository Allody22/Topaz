package ru.nsu.carwash_server.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.nsu.carwash_server.models.OperationsUserLink;
import ru.nsu.carwash_server.models.secondary.constants.DestinationPrefixes;
import ru.nsu.carwash_server.models.secondary.constants.ERole;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.exceptions.TokenRefreshException;
import ru.nsu.carwash_server.models.users.RefreshToken;
import ru.nsu.carwash_server.models.users.Role;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.models.users.UserVersions;
import ru.nsu.carwash_server.payload.request.LoginRequest;
import ru.nsu.carwash_server.payload.request.MessagesSms;
import ru.nsu.carwash_server.payload.request.SignupRequest;
import ru.nsu.carwash_server.payload.request.Smooth;
import ru.nsu.carwash_server.payload.request.SmsRequest;
import ru.nsu.carwash_server.payload.request.TokenRefreshRequest;
import ru.nsu.carwash_server.payload.response.JwtResponse;
import ru.nsu.carwash_server.payload.response.MessageResponse;
import ru.nsu.carwash_server.payload.response.TokenRefreshResponse;
import ru.nsu.carwash_server.repository.users.RoleRepository;
import ru.nsu.carwash_server.security.jwt.JwtUtils;
import ru.nsu.carwash_server.services.OperationsService;
import ru.nsu.carwash_server.services.RefreshTokenService;
import ru.nsu.carwash_server.services.UserDetailsImpl;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final RoleRepository roleRepository;

    private final PasswordEncoder encoder;

    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    private final UserService userService;

    private RestTemplate restTemplate;

    private final OperationsService operationsService;

    @Autowired
    public AuthController(
            RestTemplate restTemplate,
            UserService userService,
            OperationsService operationsService,
            AuthenticationManager authenticationManager,
            RoleRepository roleRepository,
            PasswordEncoder encoder,
            JwtUtils jwtUtils,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.operationsService = operationsService;
        this.userService = userService;
        this.restTemplate = restTemplate;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/number_check_v1")
    @SendTo(DestinationPrefixes.NOTIFICATIONS)
    @Transactional
    public ResponseEntity<?> numberCheck(@Valid @RequestParam("number") String number) throws JsonProcessingException {
        if (userService.existByUsername(number)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: телефон уже занят!"));
        }
        String smsServerUrl = "https://lcab.smsint.ru/json/v1.0/sms/send/text";

        Pair<HttpEntity<String>, Integer> resultOfSmsCreating = operationsService.createSMS(number);

        HttpEntity<String> entity = resultOfSmsCreating.getFirst();

        ResponseEntity<String> smsResponse = restTemplate.postForEntity(smsServerUrl, entity, String.class);

        // Проверка ответа от SMS сервера
        if (smsResponse.getStatusCode() == HttpStatus.OK) {
            String operationName = "User_get_phone_code";
            String descriptionMessage = "Номер телефона:'" + number + "' получил код:" + resultOfSmsCreating.getSecond();
            operationsService.SaveUserOperation(operationName, null, descriptionMessage, 1);
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка на стороне сервера для отправки смс: "
                    + smsResponse.getBody()));
        }

        return ResponseEntity.ok(new MessageResponse("Код успешно отправлен"));
    }

    @GetMapping("/getRoles")
    public ResponseEntity<?> getAllRoles() {
        return ResponseEntity.ok(roleRepository.getAllBy());
    }

    @PostMapping("/signin_v1")
    @SendTo(DestinationPrefixes.NOTIFICATIONS)
    @Transactional
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        if (!userService.existByUsername(loginRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: такого пользователя не существует!"));
        }

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userService.getFullUserById(userDetails.getId());

        String jwt = jwtUtils.generateJwtToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        String operationName = "User_sign_in";
        String descriptionMessage = "Пользователь с логином '" + loginRequest.getUsername() + "' зашёл в аккаунт";
        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        JwtResponse jwtResponse = JwtResponse
                .builder()
                .token(jwt)
                .type("Bearer")
                .refreshToken(refreshToken.getToken())
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .fullName(userService.getActualUserVersionById(user.getId()).getFullName())
                .roles(roles)
                .build();
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/signup_v1")
    @SendTo(DestinationPrefixes.NOTIFICATIONS)
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userService.existByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: телефон уже занят!"));
        }

        if (!signUpRequest.getSecretCode().equals(operationsService.getLatestCodeByPhoneNumber(signUpRequest.getUsername()) + "")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка: код подтверждения не совпадает!"));
        }

        Set<Role> roles = new HashSet<>();
        User user = new User();
        user.setDateOfCreation(new Date());
        UserVersions userFirstVersion = new UserVersions();
        userFirstVersion.setPassword(encoder.encode(signUpRequest.getPassword()));
        userFirstVersion.setUsername(signUpRequest.getUsername());
        userFirstVersion.setDateOfCreation(new Date());
        userFirstVersion.setPhone(signUpRequest.getUsername());

        Set<String> strRoles = signUpRequest.getRole();
        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new NotInDataBaseException("ролей не найдена роль: ", ERole.ROLE_USER.name()));
            roles.add(userRole);
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
                    throw new RuntimeException("Error: Invalid role.");
                }
            }).collect(Collectors.toSet());

            // Добавление роли пользователя, если ее нет в списке ролей
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new NotInDataBaseException("ролей не найдена роль: ", ERole.ROLE_USER.name()));
            roles.add(userRole);
        }

        userService.saveNewUser(user, roles, 1, userFirstVersion);

        String operationName = "User_sign_up";
        String descriptionMessage = "Клиент с логином '" + signUpRequest.getUsername() + "' зарегистрировался";
        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/refreshtoken_v1")
    @Transactional
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException(request.getRefreshToken(), "Refresh token is not in database!"));

        refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();

        UserVersions latestUserVersion = userService.getActualUserVersionById(user.getId());

        String token = jwtUtils.generateTokenFromUsername(latestUserVersion.getUsername());

        return ResponseEntity.ok(new TokenRefreshResponse(token, request.getRefreshToken()));
    }


    @PostMapping("/signout_v1")
    @Transactional
    public ResponseEntity<?> logoutUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getFullUserById(userDetails.getId());

        refreshTokenService.deleteAllByUserId(user.getId());

        Long currentUserId = userDetails.getId();
        UserVersions userLatestVersion = userService.getActualUserVersionById(currentUserId);

        String operationName = "User_sign_out";
        String descriptionMessage = "Клиент с логином '" + userLatestVersion.getUsername() + "' вышел из аккаунта";
        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }


    @PostMapping("/admin/signin_v1")
    @Transactional
    public ResponseEntity<?> signInAdmin(@Valid @RequestBody LoginRequest loginRequest) {
        if (!userService.existByUsername(loginRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: такого пользователя не существует!"));
        }

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (!roles.contains("ROLE_ADMIN") || !roles.contains("ROLE_MODERATOR") || !roles.contains("ROLE_ADMINISTRATOR")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Не достаточно прав!"));
        }

        String jwt = jwtUtils.generateJwtToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        JwtResponse jwtResponse = JwtResponse
                .builder()
                .token(jwt)
                .type("Bearer")
                .refreshToken(refreshToken.getToken())
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .roles(roles)
                .build();


        Long currentUserId = userDetails.getId();
        UserVersions userAdmin = userService.getActualUserVersionById(currentUserId);

        String operationName = "Admin_log_in";
        String descriptionMessage = "Админ с логином '" + userAdmin.getUsername() + "' зашёл в аккаунт";
        operationsService.SaveUserOperation(operationName, userAdmin.getUser(), descriptionMessage, 1);


        return ResponseEntity.ok(jwtResponse);
    }
}