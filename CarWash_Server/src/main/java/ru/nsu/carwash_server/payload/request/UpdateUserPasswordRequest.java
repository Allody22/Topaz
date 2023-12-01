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
@Schema(description = "Запрос на восстановление пароля")
public class UpdateUserPasswordRequest {

    @Schema(description = "Новый пароль", maxLength = 255)
    @Size(max = 255)
    private String password = null;

    @Schema(description = "Телефон пользователя", required = true, maxLength = 255)
    @NotBlank
    @Size(max = 30)
    private String phone = null;

    @Schema(description = "Секретный код из смс", maxLength = 4, minLength = 4)
    @Size(max = 4, min = 4)
    private String secretCode = null;

}
