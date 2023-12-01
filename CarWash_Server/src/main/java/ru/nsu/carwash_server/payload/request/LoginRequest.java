package ru.nsu.carwash_server.payload.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Класс для запроса для логина")
public class LoginRequest {

    @Schema(description = "Телефон пользователя", maxLength = 50)
    @NotBlank
    @Size(max = 50)
    private String phone = null;

    @Schema(description = "Пароль пользователя")
    @NotBlank
    private String password = null;
}
