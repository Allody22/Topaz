package ru.nsu.carwash_server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TokenRefreshResponse {

  private String accessToken;

  private String refreshToken;

  private String tokenType = "Bearer";

  public TokenRefreshResponse() {
    setTokenType("Bearer");
  }

  public TokenRefreshResponse(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
}
