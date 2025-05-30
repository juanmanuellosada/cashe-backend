package com.cashe.backend.service.mapper;

import com.cashe.backend.domain.User;
import com.cashe.backend.service.dto.RegisterRequest;
import com.cashe.backend.service.dto.UserProfileDto;
import com.cashe.backend.service.dto.UserProfileUpdateRequest;

public class UserMapper {

    public static UserProfileDto toUserProfileDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getGoogleId(),
                user.getActive(),
                user.getDefaultCurrency() != null ? user.getDefaultCurrency().getCode() : null,
                user.getPreferredLanguage(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public static User toUser(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            return null;
        }
        User user = new User();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(registerRequest.getPassword());
        user.setActive(true);
        return user;
    }

    public static void updateUserFromDto(UserProfileUpdateRequest dto, User user) {
        if (dto == null || user == null) {
            return;
        }
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getPreferredLanguage() != null) {
            user.setPreferredLanguage(dto.getPreferredLanguage());
        }
    }
}