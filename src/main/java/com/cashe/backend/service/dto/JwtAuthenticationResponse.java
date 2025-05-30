package com.cashe.backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    // Opcionalmente, podríamos incluir el refresh token aquí o en una cookie
    // HttpOnly
    // private String refreshToken;
    // Opcionalmente, tiempo de expiración del token de acceso
    // private Long expiresIn;

    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}