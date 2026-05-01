package com.ttkhnvv.rtm.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. достать токен из заголовка Authorization: Bearer <token>
        // 2. если токена нет - пропустить запрос дальше (публичный эндпоинт)
        // 3. извлечь email из токена через jwtService.extractEmail()
        // 4. загрузить пользователя через userDetailsService.loadUserByUsername()
        // 5. проверить что токен валиден через jwtService.isTokenValid()
        // 6. положить пользователя в SecurityContext
        // 7. передать запрос дальше через filterChain.doFilter()
    }
}