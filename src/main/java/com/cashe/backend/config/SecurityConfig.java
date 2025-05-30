package com.cashe.backend.config;

import com.cashe.backend.domain.User; // Necesario para el cast en successHandler
import com.cashe.backend.service.CustomOAuth2UserService;
import com.cashe.backend.service.JwtService; // Importar JwtService
import com.fasterxml.jackson.databind.ObjectMapper; // Para escribir JSON
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider; // Nuevo
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // Nuevo
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Nuevo
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService; // Nuevo
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap; // Para el cuerpo de respuesta del token
import java.util.Map; // Para el cuerpo de respuesta del token

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtService jwtService; // Inyectar JwtService
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint; // Inyectar

    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthFilter,
            @Lazy CustomOAuth2UserService customOAuth2UserService,
            @Lazy JwtService jwtService,
            @Lazy CustomAuthenticationEntryPoint customAuthenticationEntryPoint) { // Inyectar
        this.jwtAuthFilter = jwtAuthFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.jwtService = jwtService;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint; // Asignar
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Spring inyecta tu UserServiceImpl aquí
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationProvider authenticationProvider)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/auth/**", "/login/oauth2/**").permitAll() // Permitir callback OAuth2
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // Configurar aquí
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // Usar nuestro custom service
                        )
                        .successHandler((request, response, authentication) -> {
                            User userPrincipal = (User) authentication.getPrincipal();
                            String jwt = jwtService.generateToken(userPrincipal);

                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            Map<String, String> tokenResponse = new HashMap<>();
                            tokenResponse.put("accessToken", jwt);
                            tokenResponse.put("tokenType", "Bearer");

                            response.getWriter().write(new ObjectMapper().writeValueAsString(tokenResponse));
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                // Opcional: configurar failureHandler
                // .failureHandler((request, response, exception) -> { ... })
                );
        return http.build();
    }
}