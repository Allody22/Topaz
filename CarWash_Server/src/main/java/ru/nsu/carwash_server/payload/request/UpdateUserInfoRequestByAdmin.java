package ru.nsu.carwash_server.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserInfoRequestByAdmin {

    @NotBlank
    private String phone = null;

    private String fullName = null;

    private Set<String> roles = null;

    private String adminNote = null;

    private String userNote = null;

    @Email
    private String email = null;
}
