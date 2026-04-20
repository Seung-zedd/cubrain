package com.cubrain.springboot_starter_auth.global.config.web;

import com.cubrain.springboot_starter_auth.global.security.SupabaseUserSyncFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final SupabaseUserSyncFilter supabaseUserSyncFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key:${jwt.secret:}}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        // 1. Create the Asymmetric decoder (Google/Social) using Issuer Discovery
        NimbusJwtDecoder asymmetricDecoder = null;
        if (issuerUri != null && !issuerUri.trim().isEmpty()) {
            log.info("🔐 [Security] Initializing Asymmetric decoder via OIDC Discovery: {}", issuerUri);
            asymmetricDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
        }

        // 2. Create the Symmetric decoder (Email/Password) using HS256
        NimbusJwtDecoder symmetricDecoder = null;
        if (jwtSecret != null && !jwtSecret.trim().isEmpty()) {
            log.info("🔐 [Security] Initializing Symmetric decoder for HS256.");
            byte[] secretKeyBytes;
            try {
                secretKeyBytes = Base64.getDecoder().decode(jwtSecret);
            } catch (IllegalArgumentException e) {
                secretKeyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            }
            SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
            symmetricDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
        }

        if (asymmetricDecoder == null && symmetricDecoder == null) {
            throw new IllegalStateException("No JWT validation method configured! Set issuer-uri or jwt.secret.");
        }

        // 3. Common Validators
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<Object>(JwtClaimNames.AUD,
                aud -> {
                    if (aud == null)
                        return false;
                    if (aud instanceof String s)
                        return s.equals("authenticated");
                    if (aud instanceof List<?> l)
                        return l.contains("authenticated");
                    return false;
                });

        OAuth2TokenValidator<Jwt> combinedValidator;
        if (issuerUri != null && !issuerUri.trim().isEmpty()) {
            OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
            combinedValidator = new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator);
        } else {
            combinedValidator = audienceValidator;
        }

        if (asymmetricDecoder != null)
            asymmetricDecoder.setJwtValidator(combinedValidator);
        if (symmetricDecoder != null)
            symmetricDecoder.setJwtValidator(combinedValidator);

        // 4. Hybrid Decoder Logic
        final NimbusJwtDecoder finalAsym = asymmetricDecoder;
        final NimbusJwtDecoder finalSym = symmetricDecoder;

        return token -> {
            try {
                String header = token.split("\\.")[0];
                String decodedHeader = new String(Base64.getUrlDecoder().decode(header));

                if (decodedHeader.contains("HS256") && finalSym != null) {
                    return finalSym.decode(token);
                } else if (finalAsym != null) {
                    // This will now handle ES256/RS256 automatically via discovery
                    return finalAsym.decode(token);
                } else {
                    log.error("❌ [Security] Unsupported algorithm or missing decoder for token header: {}",
                            decodedHeader);
                    throw new RuntimeException("Unsupported algorithm or missing decoder");
                }
            } catch (Exception e) {
                log.error("❌ [Security] JWT Decoding failed: {}", e.getMessage());
                throw new org.springframework.security.oauth2.jwt.JwtException("Invalid token", e);
            }
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            log.error("❌ [Security] Unauthorized request: {} {} - Reason: {}",
                    request.getMethod(), request.getRequestURI(), authException.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/webhooks/**", "/api/v1/**"))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint()))
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/api/webhooks/**").permitAll()
                            .requestMatchers("/api/v1/auth/**", "/error").permitAll()
                            .requestMatchers("/", "/index.html", "/favicon.ico", "/manifest.json", "/robots.txt",
                                    "/site.webmanifest")
                            .permitAll()
                            .requestMatchers("/static/**", "/assets/**", "/_app/**", "/_vercel/**").permitAll()
                            .requestMatchers("/dashboard", "/dashboard/**").permitAll()
                            .requestMatchers("/api/v1/pdf/**", "/api/v1/cards/**", "/api/v1/waitlist/**",
                                    "/api/v1/sse/subscribe/**", "/api/v1/subscription/early-bird-count")
                            .permitAll();

                    // Swagger/OpenAPI (Access controlled by springdoc.api-docs.enabled in YML)
                    auth.requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                            .permitAll();

                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(bearerTokenResolver())
                        .jwt(jwt -> jwt.decoder(jwtDecoder())))
                .addFilterAfter(supabaseUserSyncFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    private org.springframework.security.oauth2.server.resource.web.BearerTokenResolver bearerTokenResolver() {
        org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver defaultResolver = new org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver();
        return request -> {
            String token = defaultResolver.resolve(request);
            if (token != null)
                return token;

            // Fallback to cookie
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (jakarta.servlet.http.Cookie cookie : cookies) {
                    if ("sb-access-token".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            return null;
        };
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            // Ignore Swagger/OpenAPI paths to completely bypass security filters
            web.ignoring().requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
