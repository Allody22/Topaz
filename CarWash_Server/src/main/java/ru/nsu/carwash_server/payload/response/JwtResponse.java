package ru.nsu.carwash_server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class JwtResponse {

	private String token;

	private String type = "Bearer";

	private String refreshToken;

	private Long id;

	private String username;

	private List<String> roles;
}
