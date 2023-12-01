package ru.nsu.carwash_server.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на обновление информации о пользователе админом")
public class UpdateUserInfoRequest {

    @Schema(description = "Контакты пользователя", maxLength = 50)
    @NotBlank
    @Size(max = 50)
    private String phone = null;

    @Schema(description = "ФИО пользователя", maxLength = 80)
    @Size(max = 80)
    private String fullName = null;

    @Schema(description = "Новый набор ролей пользователя")
    private Set<String> roles = null;

    @Schema(description = "Заметка администратора о пользователе")
    private String adminNote = null;

    @Schema(description = "Почта пользователя")
    private String email = null;
}
