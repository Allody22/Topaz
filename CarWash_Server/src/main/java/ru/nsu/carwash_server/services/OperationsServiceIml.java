package ru.nsu.carwash_server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ru.nsu.carwash_server.exceptions.BadRequestException;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.models.operations.OperationsUserLink;
import ru.nsu.carwash_server.models.operations.OperationsVersions;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.payload.request.MessagesSms;
import ru.nsu.carwash_server.payload.request.Smooth;
import ru.nsu.carwash_server.payload.request.SmsRequest;
import ru.nsu.carwash_server.payload.response.UserOperationsResponse;
import ru.nsu.carwash_server.repository.operations.OperationsUsersLinkRepository;
import ru.nsu.carwash_server.repository.operations.OperationsVersionsRepository;
import ru.nsu.carwash_server.services.interfaces.OperationService;
import ru.nsu.carwash_server.services.interfaces.UserService;

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

    private final UserService userServiceImp;

    @Autowired
    public OperationsServiceIml(OperationsVersionsRepository operationsVersionsRepository,
                                UserServiceImp userServiceImp,
                                OperationsUsersLinkRepository operationsUsersLinkRepository) {
        this.userServiceImp = userServiceImp;
        this.operationsUsersLinkRepository = operationsUsersLinkRepository;
        this.operationsVersionsRepository = operationsVersionsRepository;
    }

    public List<OperationsUserLink> getAllOperations() {
        return operationsUsersLinkRepository.findAll();
    }

    public List<OperationsUserLink> getAllOperationsInATime(Date startTime, Date endTime) {
        return operationsUsersLinkRepository.findAllByCreationTimeBetween(startTime, endTime);
    }

    public void checkUserSMS(String number) {
        if (getAllDescriptionOperationsByTime(number, "получил код:", LocalDateTime.now().minusHours(1)).size() >= 2) {
            throw new BadRequestException("Уже было отправлено больше двух запросов в час");
        }
    }


    public Optional<OperationsUserLink> findLatestByPhoneInLastHours(String phoneNumber, String advice, int hourNumber) {
        return operationsUsersLinkRepository.findLatestByDescriptionContainingWithAdviceInLastHour(phoneNumber,
                advice, LocalDateTime.now().minusHours(hourNumber));
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
        return operationsUsersLinkRepository.findAllByDescriptionContainingWithAdvice(phoneNumber, descriptionMessage, time);
    }

    public Pair<HttpEntity<String>, Integer> createSmsCode(String number) throws JsonProcessingException {

        //Смотрим сколько раз человек с таким phone уже получал код и если больше 2 раз за час, то не даём код
        checkUserSMS(number);

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
        Optional<OperationsUserLink> operationsUserLinkOptional = findLatestByPhoneInLastHours(phoneNumber, "получил код:", 1);
        if (operationsUserLinkOptional.isPresent()) {
            String description = operationsUserLinkOptional.get().getDescription();
            return extractCodeFromDescription(description);
        }
        throw new IllegalArgumentException("Объект с таким номером телефона не найден");
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


    public List<OperationsVersions> getAllUserOperationsByIdOrPhone(Long id, String phone) {
        if (id != null) {
            return operationsUsersLinkRepository.findAllOperationsByUserId(id);
        }
        Long userIdByName = userServiceImp.getActualUserVersionByPhone(phone).getUser().getId();
        if (userIdByName != null) {
            return operationsUsersLinkRepository.findAllOperationsByUserId(userIdByName);
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
