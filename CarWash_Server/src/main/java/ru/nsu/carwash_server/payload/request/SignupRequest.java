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
@Schema(description = "Класс для запроса для регистрации")
public class SignupRequest {

    @Schema(description = "Телефон пользователя", maxLength = 50)
    @NotBlank
    @Size(max = 50)
    private String phone = null;

    @Schema(description = "Пароль пользователя")
    @NotBlank
    private String password = null;

    @Schema(description = "Секретный код для подтверждения номера", maxLength = 4, minLength = 4)
    @Size(min = 4, max = 4)
    @NotBlank
    private String secretCode = null;

}
