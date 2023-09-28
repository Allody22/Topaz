package ru.nsu.carwash_server.payload.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//Каждый запрос должен также содержать заголовок X-Token,
// содержащий выданный вам в личном кабинете токен.
//Пример заголовка:
//X-Token: abcde12345
@Data
public class SmsRequest {
    private List<MessagesSms> messages = new ArrayList<>(); // изменено на List
    private List<String> tags = new ArrayList<>();
    private Boolean validate;
    private String startDateTime = "";
    private String timeZone = "";
    private boolean duplicateRecipientsAllowed;
    private List<String> opsosAllowed = new ArrayList<>();
    private List<String> opsosDisallowed = new ArrayList<>();
    private Integer channel = 0;
    private boolean transliterate;
    private Smooth smooth;
}