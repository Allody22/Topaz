package ru.nsu.carwash_server.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "Класс для возвращении информации об обновлении токенов доступа пользователя")
public class TokenRefreshResponse {

    @Schema(description = "JWT")
    private String accessToken;

    @Schema(description = "Рефреш токен")
    private String refreshToken;

    @Schema(description = "Тип токена")
    private String tokenType = "Bearer";

    public TokenRefreshResponse() {
        setTokenType("Bearer");
    }

    public TokenRefreshResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
