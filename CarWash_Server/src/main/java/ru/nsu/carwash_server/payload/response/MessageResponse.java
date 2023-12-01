package ru.nsu.carwash_server.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Универсальное сообщения для ответа на запросы")
public class MessageResponse {

    @Schema(description = "Само поле для сообщения")
    private String message;
}
