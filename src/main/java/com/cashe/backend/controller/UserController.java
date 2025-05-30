package com.cashe.backend.controller;

import com.cashe.backend.domain.User;
import com.cashe.backend.service.UserService;
import com.cashe.backend.service.dto.UserProfileDto;
import com.cashe.backend.service.dto.UserProfileUpdateRequest;
import com.cashe.backend.service.dto.ChangePasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(@AuthenticationPrincipal User currentUser) {
        Optional<UserProfileDto> userProfileDtoOpt = userService.getCurrentAuthenticatedUserDto();
        return userProfileDtoOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest updateRequest,
            @AuthenticationPrincipal User currentUser) {
        UserProfileDto updatedProfile = userService.updateUserProfile(currentUser.getId(), updateRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest,
            @AuthenticationPrincipal User currentUser) {
        userService.changePassword(currentUser.getId(), changePasswordRequest);
        return ResponseEntity.ok().build();
    }

    // TODO: Considerar endpoint DELETE /me para eliminación de cuenta
    // @DeleteMapping("/me")
    // @PreAuthorize("isAuthenticated()")
    // public ResponseEntity<Void> deleteOwnAccount(@AuthenticationPrincipal User
    // currentUser) {
    // userService.deleteUser(currentUser.getId());
    // return ResponseEntity.noContent().build();
    // }
}