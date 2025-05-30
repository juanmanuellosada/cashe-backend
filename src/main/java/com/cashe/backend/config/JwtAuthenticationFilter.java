package com.cashe.backend.config;

import com.cashe.backend.service.JwtService;
import com.cashe.backend.service.UserServiceImpl; // O UserDetailsService si separamos la interfaz
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserServiceImpl userDetailsService; // Usamos UserServiceImpl directamente ya que implementa
                                                      // UserDetailsService

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(JwtConstants.HEADER_STRING);
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith(JwtConstants.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response); // Si no hay token o no es Bearer, pasar al siguiente filtro
            return;
        }

        jwt = authHeader.substring(JwtConstants.TOKEN_PREFIX.length());
        try {
            userEmail = jwtService.extractUsername(jwt);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No se necesitan credenciales aquí porque el token ya está validado
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Podríamos loggear la excepción aquí (e.g., token expirado, malformado)
            // logger.error("Cannot set user authentication: {}", e);
            // No relanzamos la excepción para permitir que la cadena de filtros continúe,
            // y el acceso será denegado por las reglas de autorización si la autenticación
            // no se establece.
            logger.warn("JWT token processing error: {} - Token: {}", e.getMessage(), jwt);
        }
        filterChain.doFilter(request, response);
    }
}