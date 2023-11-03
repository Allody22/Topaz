package ru.nsu.carwash_server.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
import ru.nsu.carwash_server.configuration.security.jwt.JwtUtils;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.exceptions.SMSException;
import ru.nsu.carwash_server.exceptions.TokenRefreshException;
import ru.nsu.carwash_server.exceptions.TooManyAttemptsInCodeException;
import ru.nsu.carwash_server.exceptions.UserAlreadyExistException;
import ru.nsu.carwash_server.exceptions.UserNotFoundException;
import ru.nsu.carwash_server.models.secondary.constants.ERole;
import ru.nsu.carwash_server.models.users.RefreshToken;
import ru.nsu.carwash_server.models.users.Role;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.models.users.UserVersions;
import ru.nsu.carwash_server.payload.request.LoginRequest;
import ru.nsu.carwash_server.payload.request.SignupRequest;
import ru.nsu.carwash_server.payload.request.TokenRefreshRequest;
import ru.nsu.carwash_server.payload.response.JwtResponse;
import ru.nsu.carwash_server.payload.response.MessageResponse;
import ru.nsu.carwash_server.payload.response.TokenRefreshResponse;
import ru.nsu.carwash_server.repository.users.RoleRepository;
import ru.nsu.carwash_server.services.RefreshTokenService;
import ru.nsu.carwash_server.services.UserDetailsImpl;
import ru.nsu.carwash_server.services.interfaces.OperationService;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final RoleRepository roleRepository;

    private final PasswordEncoder encoder;

    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    private final UserService userService;

    private RestTemplate restTemplate;

    private final OperationService operationsService;


    @Autowired
    public AuthController(
            RestTemplate restTemplate,
            UserService userService,
            OperationService operationsService,
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

    @GetMapping("/generate_code_v1")
    @Transactional
    public ResponseEntity<MessageResponse> numberCheck(@Valid @NotBlank @RequestParam("number") String number, @Valid @NotBlank @RequestParam("context") String context) throws JsonProcessingException {

        //Смотрим сколько раз человек с таким phone уже получал код и если больше 2 раз за 60 минут для регистрации или смены пароля, то не даём код
        String contextMessage;
        if (context.equals("пароль")) {
            contextMessage = "' для восстановления пароля получил код:";

            if (operationsService.getAllDescriptionOperationsByTime(number, contextMessage, LocalDateTime.now().minusMinutes(60)).size() >= 2) {
                throw new SMSException();
            }

            if (!userService.existByPhone(number)) {
                return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Сначала необходимо зарегистрироваться"));
            }
        } else {
            contextMessage = "' для регистрации получил код:";

            if (operationsService.getAllDescriptionOperationsByTime(number, contextMessage, LocalDateTime.now().minusMinutes(60)).size() >= 2) {
                throw new SMSException();
            }
        }

        String smsServerUrl = "https://lcab.smsint.ru/json/v1.0/sms/send/text";
        Pair<HttpEntity<String>, Integer> resultOfSmsCreating = operationsService.createSmsCode(number);

        HttpEntity<String> entity = resultOfSmsCreating.getFirst();

        ResponseEntity<String> smsResponse = restTemplate.postForEntity(smsServerUrl, entity, String.class);

        // Проверка ответа от SMS сервера
        if (smsResponse.getStatusCode() == HttpStatus.OK) {
            //Отслеживаем рандомное число так, чтобы не тратить деньги на смс
            log.info("generate_code_v1 .Random number is: '{}'", resultOfSmsCreating.getSecond());

            String operationName = "User_get_phone_code";
            String descriptionMessage = "Номер телефона:'" + number + contextMessage + resultOfSmsCreating.getSecond();
            operationsService.SaveUserOperation(operationName, null, descriptionMessage, 1);
        } else {
            log.warn("generate_code_v1 .SmsInt failure");

            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка на стороне сервера для отправки смс: " + smsResponse.getBody()));
        }

        return ResponseEntity.ok(new MessageResponse("Код успешно отправлен"));
    }

    @GetMapping("/getRoles")
    public ResponseEntity<Optional<List<String>>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.getAllBy());
    }

    @PostMapping("/signin_v1")
    @Transactional
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String userPhone = loginRequest.getPhone();
        if (!userService.existByPhone(userPhone)) {
            throw new UserNotFoundException();
        }
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(userPhone, loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            User user = userService.getFullUserById(userDetails.getId());

            String jwt = jwtUtils.generateJwtToken(userDetails);
            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            JwtResponse jwtResponse = JwtResponse
                    .builder()
                    .token(jwt)
                    .type("Bearer")
                    .refreshToken(refreshToken.getToken())
                    .id(userDetails.getId())
                    .phone(userDetails.getUsername())
                    .fullName(userService.getActualUserVersionById(user.getId()).getFullName())
                    .roles(roles)
                    .build();
            return ResponseEntity.ok(jwtResponse);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Пожалуйста, проверьте введённые данные."));
        }
    }


    @PostMapping("/signup_v1")
    @Transactional
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        String userPhone = signUpRequest.getPhone();
        if (userService.existByPhone(userPhone)) {
            log.warn("SignUp_v1.Registration failed: Phone '{}' is already taken", userPhone);
            throw new UserAlreadyExistException();
        }

        if (operationsService.getAllDescriptionOperationsByTime(userPhone, "ввёл неверный код подтверждения для регистрации", LocalDateTime.now().minusMinutes(30)).size() >= 4) {
            log.warn("SignUp_v1.Registration failed: A lot of code attempts ");
            throw new TooManyAttemptsInCodeException();
        }

        if (!signUpRequest.getSecretCode().equals(operationsService.getLatestCodeByPhoneNumber(userPhone) + "")) {
            log.warn("SignUp_v1.Registration failed: Bad code ");
            String operationName = "User_write_wrong_code";
            String descriptionMessage = "Номер телефона '" + userPhone + "' ввёл неверный код подтверждения для регистрации";
            operationsService.SaveUserOperation(operationName, null, descriptionMessage, 1);
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Код подтверждения не совпадает!"));
        }

        Set<Role> roles = new HashSet<>();
        User user = new User();
        user.setDateOfCreation(new Date());
        UserVersions userFirstVersion = new UserVersions();
        userFirstVersion.setPassword(encoder.encode(signUpRequest.getPassword()));
        userFirstVersion.setPhone(signUpRequest.getPhone());
        userFirstVersion.setDateOfCreation(new Date());
        userFirstVersion.setPhone(userPhone);

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new NotInDataBaseException("ролей не найдена роль: ", ERole.ROLE_USER.name()));
        roles.add(userRole);

        userService.saveNewUser(user, roles, 1, userFirstVersion);

        String operationName = "User_sign_up";
        String descriptionMessage = "Клиент с логином '" + userPhone + "' зарегистрировался";
        operationsService.SaveUserOperation(operationName, user, descriptionMessage, 1);

        log.info("SignUp_v1.User with phone '{}' registered successfully", userPhone);

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

        String token = jwtUtils.generateTokenFromUsername(latestUserVersion.getPhone());

        return ResponseEntity.ok(new TokenRefreshResponse(token, request.getRefreshToken()));
    }


    @PostMapping("/signout_v1")
    @Transactional
    public ResponseEntity<MessageResponse> logoutUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getFullUserById(userDetails.getId());

        refreshTokenService.deleteAllByUserId(user.getId());

        return ResponseEntity.ok(new MessageResponse("Успешный выход из аккаунта!"));
    }


    @PostMapping("/admin/signin_v1")
    @Transactional
    public ResponseEntity<?> signInAdmin(@Valid @RequestBody LoginRequest loginRequest) {
        if (!userService.existByPhone(loginRequest.getPhone())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такого пользователя не существует!"));
        }

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getPhone(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (!roles.contains("ROLE_ADMIN") || !roles.contains("ROLE_MODERATOR") || !roles.contains("ROLE_ADMINISTRATOR")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка: Не достаточно прав!"));
        }

        String jwt = jwtUtils.generateJwtToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        JwtResponse jwtResponse = JwtResponse
                .builder()
                .token(jwt)
                .type("Bearer")
                .refreshToken(refreshToken.getToken())
                .id(userDetails.getId())
                .phone(userDetails.getUsername())
                .roles(roles)
                .build();

        return ResponseEntity.ok(jwtResponse);
    }
}