package com.example.photobook.config;

import com.example.photobook.service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:5175,https://photobookvue-production.up.railway.app}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/socket.io/**").permitAll()
                        .requestMatchers("/uploads-storage/**").permitAll()
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/me").authenticated()

                        .requestMatchers("/api/v1/roles/**")
                        .hasAuthority("ROLE_ADMIN")

                        .requestMatchers(
                                "/api/v1/customers/**",
                                "/api/v1/materials/**",
                                "/api/v1/expense-categories/**",
                                "/api/v1/expenses/**",
                                "/api/v1/product-categories/**"
                        ).hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")

                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")

                        .requestMatchers(HttpMethod.GET, "/api/v1/dashboard/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")

                        .requestMatchers(HttpMethod.GET, "/api/v1/orders/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_OPERATOR")

                        .requestMatchers("/api/v1/user-tasks/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_OPERATOR")

                        .requestMatchers("/api/v1/notifications/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_OPERATOR")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/orders/*/status")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_OPERATOR")

                        .requestMatchers("/api/v1/orders/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")

                        .requestMatchers("/api/v1/uploads/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_OPERATOR")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .collect(Collectors.toList()));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    public static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };
}
