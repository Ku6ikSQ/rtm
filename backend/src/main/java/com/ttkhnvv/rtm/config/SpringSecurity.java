package com.ttkhnvv.rtm.config;

import com.ttkhnvv.rtm.security.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;

@Configuration
@EnableMethodSecurity
public class SpringSecurity {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           JwtFilter jwtFilter,
                                           AuthenticationEntryPoint authEntryPoint,
                                           AccessDeniedHandler accessDeniedHandler) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                api("/auth/register"),
                                api("/auth/login")
                        ).permitAll()
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                api("/albums/"),
//                                api("/albums/{id}")
//                        ).permitAll()
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                api("/artists/{id}")
//                        ).permitAll()
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                api("/album-artists/by-album/{albumId}"),
//                                api("/album-artists/{albumId}/{artistId}")
//                        ).permitAll()
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                api("/album-genres/by-album/{albumId}"),
//                                api("/album-genres/{albumId}/{genreId}")
//                        ).permitAll()
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                api("/genres/"),
//                                api("/genres/{id}")
//                        ).permitAll()
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                api("/platforms/"),
//                                api("/platforms/{id}")
//                        ).permitAll()
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                api("/tracks/"),
//                                api("/tracks/{id}")
//                        ).permitAll()
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                api("/reviews/"),
//                                api("/reviews/{id}")
//                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex ->
                        ex
                                .authenticationEntryPoint(authEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler)
                )
                .build();
    }

    private static String api(String path) {
        return API_PREFIX + path;
    }
}