package com.cashe.backend.controller;

import com.cashe.backend.domain.Currency;
import com.cashe.backend.domain.User;
import com.cashe.backend.repository.CurrencyRepository;
import com.cashe.backend.repository.UserRepository;
import com.cashe.backend.service.dto.ChangePasswordRequest;
import com.cashe.backend.service.dto.JwtAuthenticationResponse;
import com.cashe.backend.service.dto.LoginRequest;
import com.cashe.backend.service.dto.UserProfileUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class UserControllerTest {

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

    private String testUserEmail = "testuser@example.com";
    private String testUserPassword = "password123";
    private String testUserFullName = "Test User FullName";
    private Currency defaultCurrency;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
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
        currencyRepository.findById("EUR").orElseGet(() -> {
            Currency c = new Currency();
            c.setCode("EUR");
            c.setName("Euro");
            c.setSymbol("€");
            c.setExchangeRate(new BigDecimal("0.9"));
            c.setIsBaseCurrency(false);
            c.setIsActive(true);
            return currencyRepository.save(c);
        });
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail(testUserEmail);
        user.setPasswordHash(passwordEncoder.encode(testUserPassword));
        user.setFullName(testUserFullName);
        user.setDefaultCurrency(defaultCurrency);
        user.setPreferredLanguage("es");
        user.setActive(true);
        return userRepository.save(user);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        JwtAuthenticationResponse authResponse = objectMapper.readValue(responseString,
                JwtAuthenticationResponse.class);
        return authResponse.getAccessToken();
    }

    @Test
    void getCurrentUserProfile_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUserProfile_whenAuthenticated_shouldReturnUserProfile() throws Exception {
        createTestUser();
        String token = loginAndGetToken(testUserEmail, testUserPassword);

        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(testUserEmail)))
                .andExpect(jsonPath("$.fullName", is(testUserFullName)));
    }

    @Test
    void updateUserProfile_whenAuthenticated_shouldUpdateProfile() throws Exception {
        User user = createTestUser();
        String token = loginAndGetToken(testUserEmail, testUserPassword);

        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
        updateRequest.setFullName("Updated Test User FullName");
        updateRequest.setPreferredLanguage("en");
        updateRequest.setDefaultCurrencyCode("EUR");

        mockMvc.perform(put("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Updated Test User FullName")))
                .andExpect(jsonPath("$.preferredLanguage", is("en")))
                .andExpect(jsonPath("$.defaultCurrencyCode", is("EUR")));

        Optional<User> updatedUserOpt = userRepository.findById(user.getId());
        assertTrue(updatedUserOpt.isPresent());
        User updatedUser = updatedUserOpt.get();
        assertEquals("Updated Test User FullName", updatedUser.getFullName());
        assertEquals("en", updatedUser.getPreferredLanguage());
        assertEquals("EUR", updatedUser.getDefaultCurrency().getCode());
    }

    @Test
    void changePassword_whenAuthenticatedAndOldPasswordCorrect_shouldChangePassword() throws Exception {
        User user = createTestUser();
        String token = loginAndGetToken(testUserEmail, testUserPassword);
        String newPassword = "newPassword456";

        ChangePasswordRequest changeRequest = new ChangePasswordRequest(testUserPassword, newPassword);

        mockMvc.perform(post("/api/users/me/change-password")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isOk());

        Optional<User> updatedUserOpt = userRepository.findById(user.getId());
        assertTrue(updatedUserOpt.isPresent());
        assertTrue(passwordEncoder.matches(newPassword, updatedUserOpt.get().getPasswordHash()));

        // Intentar login con la nueva contraseña
        loginAndGetToken(testUserEmail, newPassword);
    }

    @Test
    void changePassword_whenOldPasswordIncorrect_shouldReturnBadRequest() throws Exception {
        createTestUser();
        String token = loginAndGetToken(testUserEmail, testUserPassword);
        String newPassword = "newPassword456";

        ChangePasswordRequest changeRequest = new ChangePasswordRequest("wrongOldPassword", newPassword);

        mockMvc.perform(post("/api/users/me/change-password")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isBadRequest()) // OperationNotAllowedException es 400
                .andExpect(jsonPath("$.message", is("Incorrect old password")));
    }
}