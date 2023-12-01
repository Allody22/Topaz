package ru.nsu.carwash_server.payload.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Класс для запроса для обновления рефреш токена")
public class TokenRefreshRequest {

    @Schema(description = "Рефреш токен")
    @NotBlank
    private String refreshToken;
}
