package ru.nsu.carwash_server.payload.request;

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
public class UpdateUserInfoRequest {

    @NotBlank
    @Size(max = 50)
    private String phone = null;

    @Size(max = 80)
    private String fullName = null;

    private Set<String> roles = null;

    private String adminNote = null;

    private String userNote = null;

    private String email = null;
}
