package ru.nsu.carwash_server.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Schema(description = "Класс для возвращении информации о токенах доступа пользователя")
public class JwtResponse {

    @Schema(description = "JWT")
    private String token;

    @Schema(description = "Тип токена")
    private String type = "Bearer";

    @Schema(description = "Рефреш токен")
    private String refreshToken;

    @Schema(description = "Айди")
    private Long id;

    @Schema(description = "Контакты пользователя")
    private String phone;

    @Schema(description = "ФИО")
    private String fullName;

    @Schema(description = "Набор ролей пользователя ")
    private List<String> roles;
}
