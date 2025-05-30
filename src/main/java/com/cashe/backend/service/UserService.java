package com.cashe.backend.service;

import com.cashe.backend.domain.User;
import com.cashe.backend.service.dto.ChangePasswordRequest;
import com.cashe.backend.service.dto.RegisterRequest;
import com.cashe.backend.service.dto.UserProfileDto;
import com.cashe.backend.service.dto.UserProfileUpdateRequest;
// import com.cashe.backend.domain.User; // Ya no se expone la entidad directamente
import java.util.Optional;

public interface UserService {

    UserProfileDto registerUser(RegisterRequest registerRequest);

    Optional<UserProfileDto> findByEmail(String email);

    Optional<UserProfileDto> findById(Long id);

    UserProfileDto updateUserProfile(Long userId, UserProfileUpdateRequest profileUpdateRequest);

    void changePassword(Long userId, ChangePasswordRequest changePasswordRequest);

    void deleteUser(Long userId); // Mantenemos la firma, la lógica interna buscará el User

    // Métodos relacionados con OAuth (Google)
    // La firma de processGoogleUser también podría tomar un DTO si la información
    // de Google es más compleja
    UserProfileDto processGoogleUser(String googleId, String email, String fullName, String imageUrl);

    // Obtener el usuario autenticado actualmente
    Optional<UserProfileDto> getCurrentAuthenticatedUserDto(); // Renombrado para claridad

    User getCurrentAuthenticatedUserEntity(); // Nuevo método
}