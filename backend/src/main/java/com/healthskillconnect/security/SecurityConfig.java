
package com.healthskillconnect.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.healthskillconnect.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtService jwt,
            UserService users,
            @Value("${app.frontend-origin}") String origin
    ) throws Exception {

        http
            .csrf(c -> c.disable())
            .cors(c -> c.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();

                config.setAllowedOrigins(List.of(origin));
                config.setAllowedMethods(List.of(
                    "GET", "POST", "PUT", "PATCH",
                    "DELETE", "OPTIONS"
                ));
                config.setAllowedHeaders(List.of(
                    "Authorization", "Content-Type"
                ));

                return config;
            }))
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public authentication and health endpoints
                .requestMatchers(
                    "/api/auth/**",
                    "/api/health",
                    "/api/ai/health"
                ).permitAll()

                // Public read-only endpoints
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/courses/**",
                    "/api/paths/**",
                    "/api/opportunities/**",
                    "/api/resources/**"
                ).permitAll()

                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, exception) ->
                    response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentication required"
                    )
                )
                .accessDeniedHandler((request, response, exception) ->
                    response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden"
                    )
                )
            );

        http.addFilterBefore(
            new OnceJwtFilter(jwt, users),
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    static class OnceJwtFilter
            extends org.springframework.web.filter.OncePerRequestFilter {

        private final JwtService jwt;
        private final UserService users;

        OnceJwtFilter(JwtService jwt, UserService users) {
            this.jwt = jwt;
            this.users = users;
        }

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain chain
        ) throws ServletException, IOException {

            String authorization = request.getHeader("Authorization");

            if (authorization != null
                    && authorization.startsWith("Bearer ")) {

                try {
                    var claims = jwt.parse(
                        authorization.substring(7)
                    );

                    var user = users.byId(claims.getSubject());

                    if (user != null
                            && user.role.equals(
                                claims.get("role", String.class)
                            )) {

                        request.setAttribute("authUser", user);

                        var authentication =
                            new UsernamePasswordAuthenticationToken(
                                user.id,
                                null,
                                List.of(
                                    new SimpleGrantedAuthority(
                                        "ROLE_" + user.role
                                    )
                                )
                            );

                        org.springframework.security.core.context
                            .SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                    }

                } catch (Exception ignored) {
                    // Invalid or expired JWT: continue without authentication.
                    // Protected endpoints will be rejected by Spring Security.
                }
            }

            chain.doFilter(request, response);
        }
    }
}
