package com.cashe.backend.service;

import com.cashe.backend.common.exception.OperationNotAllowedException;
import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.domain.Currency;
import com.cashe.backend.domain.User;
import com.cashe.backend.repository.CurrencyRepository;
import com.cashe.backend.repository.UserRepository;
import com.cashe.backend.service.dto.ChangePasswordRequest;
import com.cashe.backend.service.dto.RegisterRequest;
import com.cashe.backend.service.dto.UserProfileDto;
import com.cashe.backend.service.dto.UserProfileUpdateRequest;
import com.cashe.backend.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final @Lazy PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Override
    @Transactional
    public UserProfileDto registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new OperationNotAllowedException("Error: Email is already in use!");
        }
        User newUser = UserMapper.toUser(registerRequest);
        newUser.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        String currencyCode = "ARS";
        Currency defaultCurrency = currencyRepository.findById(currencyCode)
                .orElseThrow(() -> new ResourceNotFoundException("Default currency", "code", currencyCode));
        newUser.setDefaultCurrency(defaultCurrency);
        newUser.setPreferredLanguage("es");

        User registeredUser = userRepository.save(newUser);
        return UserMapper.toUserProfileDto(registeredUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDto> findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::toUserProfileDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDto> findById(Long id) {
        return userRepository.findById(id).map(UserMapper::toUserProfileDto);
    }

    @Override
    @Transactional
    public UserProfileDto updateUserProfile(Long userId, UserProfileUpdateRequest profileUpdateRequest) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserMapper.updateUserFromDto(profileUpdateRequest, existingUser);

        if (profileUpdateRequest.getDefaultCurrencyCode() != null &&
                (existingUser.getDefaultCurrency() == null || !existingUser.getDefaultCurrency().getCode()
                        .equals(profileUpdateRequest.getDefaultCurrencyCode()))) {
            Currency newDefaultCurrency = currencyRepository.findById(profileUpdateRequest.getDefaultCurrencyCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Currency", "code", profileUpdateRequest.getDefaultCurrencyCode()));
            existingUser.setDefaultCurrency(newDefaultCurrency);
        }

        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toUserProfileDto(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest changePasswordRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPasswordHash())) {
            throw new OperationNotAllowedException("Incorrect old password");
        }
        user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileDto processGoogleUser(String googleId, String email, String fullName, String imageUrl) {
        Optional<User> existingUserOpt = userRepository.findByGoogleId(googleId);
        if (existingUserOpt.isPresent()) {
            return UserMapper.toUserProfileDto(existingUserOpt.get());
        }

        Optional<User> userByEmailOpt = userRepository.findByEmail(email);
        User userToProcess;
        if (userByEmailOpt.isPresent()) {
            userToProcess = userByEmailOpt.get();
            userToProcess.setGoogleId(googleId);
        } else {
            userToProcess = new User();
            userToProcess.setEmail(email);
            userToProcess.setFullName(fullName);
            userToProcess.setGoogleId(googleId);
            userToProcess.setActive(true);

            String currencyCode = "USD"; // Placeholder
            Currency defaultCurrency = currencyRepository.findById(currencyCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Default currency", "code", currencyCode));
            userToProcess.setDefaultCurrency(defaultCurrency);
            userToProcess.setPreferredLanguage("es"); // Placeholder
        }
        User processedUser = userRepository.save(userToProcess);
        return UserMapper.toUserProfileDto(processedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDto> getCurrentAuthenticatedUserDto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn(
                    "Attempted to get current authenticated user DTO, but no authenticated user found or principal is anonymous.");
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(username).map(UserMapper::toUserProfileDto);
        } else if (principal instanceof String) {
            return userRepository.findByEmail((String) principal).map(UserMapper::toUserProfileDto);
        }
        logger.warn("Attempted to get current authenticated user DTO - principal is not UserDetails or String: {}",
                principal.getClass().getName());
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("No authenticated user found or principal is anonymous.");
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new IllegalStateException(
                    "Principal is not UserDetails or String: " + principal.getClass().getName());
        }
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Authenticated user not found in repository with email: " + username));
    }
}