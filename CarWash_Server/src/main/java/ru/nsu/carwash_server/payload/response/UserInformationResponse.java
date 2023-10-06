package ru.nsu.carwash_server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInformationResponse {

    private Long id;

    private String fullName;

    private String phone;

    private String email;

    private int bonuses;

    private Set<String> roles;

    private String userNotes;

    private String adminNotes;
}
