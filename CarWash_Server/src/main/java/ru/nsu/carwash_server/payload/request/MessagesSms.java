package ru.nsu.carwash_server.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessagesSms {

    private String recipient = "";

    private String text = "";

    private String recipientType = "";

    private String id;

    private String source;

    private Integer timeout;

    private Boolean shortenUrl;
}
