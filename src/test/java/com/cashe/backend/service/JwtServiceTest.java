package com.cashe.backend.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;
    private UserDetails otherUserDetails;

    @BeforeEach
    void setUp() {
        userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            }

            @Override
            public String getPassword() {
                return "password";
            }

            @Override
            public String getUsername() {
                return "testuser@example.com";
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };

        otherUserDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            }

            @Override
            public String getPassword() {
                return "password";
            }

            @Override
            public String getUsername() {
                return "otheruser@example.com";
            } // Diferente username

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken(userDetails);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    void generateTokenWithExtraClaims_shouldContainExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", 1L);
        String token = jwtService.generateToken(extraClaims, userDetails);
        assertNotNull(token);
        Long userIdClaim = jwtService.extractClaim(token, claims -> claims.get("userId", Long.class));
        assertEquals(1L, userIdClaim);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    void isTokenValid_withValidTokenAndCorrectUser_shouldReturnTrue() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_withValidTokenAndDifferentUser_shouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);
        assertFalse(jwtService.isTokenValid(token, otherUserDetails));
    }

    @Test
    void isTokenValid_withExpiredToken_shouldReturnFalse() {
        Map<String, Object> claims = new HashMap<>();
        String expiredToken = jwtService.generateTokenWithCustomExpiration(claims, userDetails,
                new Date(System.currentTimeMillis() - 10000));
        assertFalse(jwtService.isTokenValid(expiredToken, userDetails));
    }

    @Test
    void extractUsername_withMalformedToken_shouldThrowException() {
        String malformedToken = "this.is.not.a.jwt";
        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername(malformedToken));
    }

    @Test
    void extractUsername_withInvalidSignatureToken_shouldThrowException() {
        String token = jwtService.generateToken(userDetails);
        String[] parts = token.split("\\.");
        String invalidSignatureToken = parts[0] + "." + parts[1] + ".invalidSignature123";
        assertThrows(SignatureException.class, () -> jwtService.extractUsername(invalidSignatureToken));
    }

    @Test
    void extractUsername_withExpiredToken_shouldThrowException() {
        Map<String, Object> claims = new HashMap<>();
        String expiredToken = jwtService.generateTokenWithCustomExpiration(claims, userDetails,
                new Date(System.currentTimeMillis() - 10000));
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(expiredToken));
    }

    // Helper en JwtService para permitir generar token con expiración customizada
    // para tests
    // Este método NO debería estar en el JwtService de producción.
    // Solo para testability, o usar una librería de manipulación de tiempo.
    // Por ahora, lo añadiré a JwtService y luego lo marco como solo para test.
}