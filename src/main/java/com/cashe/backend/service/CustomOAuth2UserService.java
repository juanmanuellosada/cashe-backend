package com.cashe.backend.service;

import com.cashe.backend.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String googleId = oauth2User.getName();
        String email = oauth2User.getAttribute("email");
        String fullName = oauth2User.getAttribute("name");
        String imageUrl = oauth2User.getAttribute("picture");

        userService.processGoogleUser(googleId, email, fullName, imageUrl);

        User appUser = (User) userDetailsService.loadUserByUsername(email);

        appUser.setAttributes(oauth2User.getAttributes());

        return appUser;
    }
}