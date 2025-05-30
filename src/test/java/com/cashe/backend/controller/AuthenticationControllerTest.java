package com.cashe.backend.controller;

import com.cashe.backend.domain.Currency;
import com.cashe.backend.domain.User;
import com.cashe.backend.repository.CurrencyRepository;
import com.cashe.backend.repository.UserRepository;
import com.cashe.backend.service.dto.LoginRequest;
import com.cashe.backend.service.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Cada test se ejecuta en una transacción que se revierte
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Currency defaultCurrency;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Limpiar usuarios antes de cada test
        // Asegurar que la moneda por defecto exista para el registro
        defaultCurrency = currencyRepository.findById("USD").orElseGet(() -> {
            Currency c = new Currency();
            c.setCode("USD");
            c.setName("US Dollar");
            c.setSymbol("$");
            c.setExchangeRate(BigDecimal.ONE);
            c.setIsBaseCurrency(true);
            c.setIsActive(true);
            return currencyRepository.save(c);
        });
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("Test User", "test@example.com", "password123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.fullName", is("Test User")))
                .andExpect(jsonPath("$.id", notNullValue()));

        Optional<User> savedUser = userRepository.findByEmail("test@example.com");
        assertTrue(savedUser.isPresent());
        assertTrue(passwordEncoder.matches("password123", savedUser.get().getPasswordHash()));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() throws Exception {
        // Crear un usuario inicial
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setPasswordHash(passwordEncoder.encode("password"));
        existingUser.setFullName("Existing User");
        existingUser.setDefaultCurrency(defaultCurrency);
        existingUser.setPreferredLanguage("es");
        userRepository.save(existingUser);

        RegisterRequest registerRequest = new RegisterRequest("New User", "existing@example.com", "newpassword123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest()) // Esperamos 400 por OperationNotAllowedException
                .andExpect(jsonPath("$.message", is("Error: Email is already in use!")));
    }

    @Test
    void testLoginUser_Success() throws Exception {
        User user = new User();
        user.setEmail("login@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setFullName("Login User");
        user.setDefaultCurrency(defaultCurrency);
        user.setPreferredLanguage("es");
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("login@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    void testLoginUser_UserNotFound() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()); // Spring Security devuelve 401 por defecto para fallos de
                                                       // autenticación
    }

    @Test
    void testLoginUser_IncorrectPassword() throws Exception {
        User user = new User();
        user.setEmail("wrongpass@example.com");
        user.setPasswordHash(passwordEncoder.encode("correctPassword"));
        user.setFullName("Wrong Pass User");
        user.setDefaultCurrency(defaultCurrency);
        user.setPreferredLanguage("es");
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("wrongpass@example.com", "incorrectPassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()); // Spring Security devuelve 401
    }
}