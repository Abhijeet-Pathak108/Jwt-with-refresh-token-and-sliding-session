package tech.csm.dto;

import lombok.Data;


@Data
public class AuthResponse {
    private String token;
    private String refreshToken;

    public AuthResponse(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
