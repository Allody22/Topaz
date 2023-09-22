package ru.nsu.carwash_server.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserInfoRequest {

    private String username = null;

    private String fullName = null;

    private Set<String> roles = null;

    private String adminNote = null;

    private String userNote = null;

    private String email = null;

    private String password = null;

}
