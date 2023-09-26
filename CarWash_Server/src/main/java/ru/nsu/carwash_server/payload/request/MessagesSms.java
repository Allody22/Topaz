package ru.nsu.carwash_server.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessagesSms {
        private String recipient = "";
        private String text = "";
        private String recipientType = "";
        private String id = "";
        private String source = "";
        private Integer timeout = 0;
        private Boolean shortenUrl;
}
