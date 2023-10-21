package ru.nsu.carwash_server.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserInfoRequestByAdmin {

    @NotBlank
    @Size(max = 50)
    private String phone = null;

    @Size(max = 80)
    private String fullName = null;

    private Set<String> roles = null;

    @Size(max = 120)
    private String adminNote = null;

    @Size(max = 120)
    private String userNote = null;

    @Email
    private String email = null;
}
