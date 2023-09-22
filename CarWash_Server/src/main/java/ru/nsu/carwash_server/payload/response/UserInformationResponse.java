package ru.nsu.carwash_server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nsu.carwash_server.models.orders.Order;
import ru.nsu.carwash_server.models.users.Role;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInformationResponse {

    private Set<Order> orders;

    private Long id;

    private String fullName;

    private String phone;

    private String email;

    private int bonuses;

    private Set<Role> roles;

    private String userNotes;

    private String adminNotes;
}
