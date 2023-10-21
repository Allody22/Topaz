package ru.nsu.carwash_server.payload.request;

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
public class UpdateUserPasswordRequest {

    private String password = null;

    @NotBlank
    @Size(max = 30)
    private String phone = null;

    @Size(max = 4, min = 4)
    private String secretCode = null;

}
