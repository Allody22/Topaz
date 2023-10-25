package ru.nsu.carwash_server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ru.nsu.carwash_server.exceptions.ConfirmationCodeMismatchException;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.models.operations.OperationsUserLink;
import ru.nsu.carwash_server.models.operations.OperationsVersions;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.payload.request.MessagesSms;
import ru.nsu.carwash_server.payload.request.Smooth;
import ru.nsu.carwash_server.payload.request.SmsRequest;
import ru.nsu.carwash_server.payload.response.UserOperationsResponse;
import ru.nsu.carwash_server.repository.operations.OperationsRepository;
import ru.nsu.carwash_server.repository.operations.OperationsUsersLinkRepository;
import ru.nsu.carwash_server.repository.operations.OperationsVersionsRepository;
import ru.nsu.carwash_server.services.interfaces.OperationService;
import ru.nsu.carwash_server.services.interfaces.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OperationsServiceIml implements OperationService {


    private final OperationsVersionsRepository operationsVersionsRepository;

    private final OperationsUsersLinkRepository operationsUsersLinkRepository;

    private final OperationsRepository operationsRepository;

    private final UserService userServiceImp;

    @Autowired
    public OperationsServiceIml(OperationsRepository operationsRepository,
                                OperationsVersionsRepository operationsVersionsRepository,
                                UserServiceImp userServiceImp,
                                OperationsUsersLinkRepository operationsUsersLinkRepository) {
        this.userServiceImp = userServiceImp;
        this.operationsRepository = operationsRepository;
        this.operationsUsersLinkRepository = operationsUsersLinkRepository;
        this.operationsVersionsRepository = operationsVersionsRepository;
    }

    public List<String> getAllOperationsNames() {
        return operationsRepository.findAllOperationNames();
    }

    public List<OperationsUserLink> getAllOperationsByName(String operationName) {
        return operationsUsersLinkRepository.getAllByOperationName(operationName);
    }

    public List<OperationsUserLink> getAllOperations() {
        return operationsUsersLinkRepository.findAll();
    }

    public List<OperationsUserLink> getAllOperationsInATime(Date startTime, Date endTime) {
        return operationsUsersLinkRepository.findAllByCreationTimeBetween(startTime, endTime);
    }


    public Optional<OperationsUserLink> findLatestByPhoneInLastHours(String phoneNumber, String advice, int hourNumber) {
        return operationsUsersLinkRepository.findLatestByDescriptionContainingWithAdviceInLastHour(phoneNumber,
                advice, LocalDateTime.now().minusHours(hourNumber));
    }

    public Optional<OperationsUserLink> findLatestByPhoneInMinutes(String phoneNumber, String context, int minutesNumber) {
        return operationsUsersLinkRepository.findLatestByDescriptionContainingWithAdviceInLastHour(phoneNumber,
                context, LocalDateTime.now().minusMinutes(minutesNumber));
    }

    public int extractCodeFromDescription(String description) {
        Pattern pattern = Pattern.compile("код:(\\d+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("Код не найден в описании");
    }

    public List<OperationsUserLink> getAllDescriptionOperationsByTime(String phoneNumber, String descriptionMessage, LocalDateTime time) {
        System.out.println(operationsUsersLinkRepository.findAllByDescriptionContainingWithAdvice(phoneNumber, descriptionMessage, time).toString());
        return operationsUsersLinkRepository.findAllByDescriptionContainingWithAdvice(phoneNumber, descriptionMessage, time);
    }

    @Transactional
    public Pair<HttpEntity<String>, Integer> createSmsCode(String number) throws JsonProcessingException {

        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(8999);

        LocalDateTime oneMinuteLater = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String newDate = oneMinuteLater.format(formatter);

        MessagesSms message = new MessagesSms();
        message.setRecipient(number);
        message.setText("Ваш код: " + randomNumber);
        message.setRecipientType("recipient");
        message.setId("Подтверждение телефона"); //ЧЁ ЗА АЙДИ
        message.setSource("CarWash");
        message.setTimeout(3600);
        message.setShortenUrl(true);

        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setMessages(Collections.singletonList(message));

        //При true смс не отправляется
        smsRequest.setValidate(true);

        List<String> tags = new ArrayList<>();
        tags.add("Подтверждение телефона");
        smsRequest.setTags(tags);

        smsRequest.setStartDateTime(newDate);

        LocalDateTime endDateTime = LocalDateTime.now().plusMinutes(5);
        String newDateStop = endDateTime.format(formatter);

        Smooth smooth = new Smooth();
        smooth.setStopDateTime(newDateStop);
        smooth.setStepSeconds(600);
        smsRequest.setSmooth(smooth);

        smsRequest.setTimeZone("Asia/Novosibirsk");
        smsRequest.setDuplicateRecipientsAllowed(false);

        //Добавить больше операторов
        List<String> operators = List.of(
                "beeline"
        );
        List<String> opsosAllowed = new ArrayList<>(operators);

        smsRequest.setOpsosAllowed(opsosAllowed);
        smsRequest.setOpsosDisallowed(new ArrayList<>());

        smsRequest.setChannel(0);
        smsRequest.setTransliterate(false);

        String xToken = "8iai05hceeekir0w5e3z9ntgxtg2g8net4m4f3f3b1rxyq02yxbsh633bq02iv1l";

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(smsRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Token", xToken);

        HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);

        return Pair.of(entity, randomNumber);
    }

    public int getLatestCodeByPhoneNumber(String phoneNumber) {
        OperationsUserLink operationsUserLink = findLatestByPhoneInMinutes(phoneNumber, "получил код:", 5)
                .orElseThrow(ConfirmationCodeMismatchException::new);
        String description = operationsUserLink.getDescription();
        return extractCodeFromDescription(description);
    }

    public void SaveUserOperation(String operationName, User user, String descriptionMessage, int version) {
        OperationsVersions operationsVersions = getOperationVersionByNameAndVersion(operationName, version);
        OperationsUserLink operationsUserLink = new OperationsUserLink();
        operationsUserLink.setDateOfCreation(new Date());
        operationsUserLink.setOperation(operationsVersions);
        operationsUserLink.setUser(user);

        operationsUserLink.setDescription(descriptionMessage);

        operationsUsersLinkRepository.save(operationsUserLink);
    }


    public List<OperationsUserLink> getAllUserOperationsByIdOrPhone(Long id, String phone) {
        if (id != null) {
            return operationsUsersLinkRepository.findAllByUserId(id);
        }
        Long userIdByName = userServiceImp.getActualUserVersionByPhone(phone).getUser().getId();
        if (userIdByName != null) {
            return operationsUsersLinkRepository.findAllByUserId(userIdByName);
        } else {
            return null;
        }
    }

    public OperationsVersions getOperationVersionByNameAndVersion(String name, Integer version) {
        return operationsVersionsRepository.findByOperationNameAndVersion(name, version)
                .orElseThrow(() -> new NotInDataBaseException("услуг операций не найдена операция с именем: ",
                        name + " и версией:" + version));
    }

    public List<UserOperationsResponse> getRationalOperationForm(List<OperationsVersions> operationsVersions) {
        List<UserOperationsResponse> userOperationsResponses = new ArrayList<>();
        for (var singleOperation : operationsVersions) {
            UserOperationsResponse userOperationsResponse = new UserOperationsResponse();
            userOperationsResponse.setDateOfCreation(singleOperation.getDateOfCreation());
            userOperationsResponse.setVersion(singleOperation.getVersion());
            userOperationsResponse.setName(singleOperation.getOperations().getName());
            userOperationsResponse.setChanges(singleOperation.getChanges());
            userOperationsResponses.add(userOperationsResponse);
        }
        return userOperationsResponses;
    }
}
