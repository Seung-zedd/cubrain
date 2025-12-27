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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
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

    @Value("${springdoc.api-docs.enabled:false}")
    private boolean isSwaggerEnabled;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key:${jwt.secret:}}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        // 1. Create the JWKS decoder (for ES256/RS256 - Google/Social)
        NimbusJwtDecoder jwksDecoder = null;
        if (jwkSetUri != null && !jwkSetUri.trim().isEmpty()) {
            log.info("🔐 [Security] Initializing JWKS decoder for Asymmetric tokens (Google/Social).");
            jwksDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }

        // 2. Create the SecretKey decoder (for HS256 - Email/Password)
        NimbusJwtDecoder secretKeyDecoder = null;
        if (jwtSecret != null && !jwtSecret.trim().isEmpty()) {
            log.info("🔐 [Security] Initializing SecretKey decoder for Symmetric tokens (Email/Password).");
            byte[] secretKeyBytes;
            try {
                secretKeyBytes = Base64.getDecoder().decode(jwtSecret);
            } catch (IllegalArgumentException e) {
                secretKeyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            }
            SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
            secretKeyDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
        }

        if (jwksDecoder == null && secretKeyDecoder == null) {
            throw new IllegalStateException("No JWT validation method configured!");
        }

        // 3. Create a Hybrid Decoder that checks the 'alg' header
        final NimbusJwtDecoder finalJwks = jwksDecoder;
        final NimbusJwtDecoder finalSecret = secretKeyDecoder;

        JwtDecoder hybridDecoder = token -> {
            try {
                // Peek at the header to see the algorithm
                String header = token.split("\\.")[0];
                String decodedHeader = new String(Base64.getUrlDecoder().decode(header));

                if (decodedHeader.contains("HS256") && finalSecret != null) {
                    return finalSecret.decode(token);
                } else if (finalJwks != null) {
                    return finalJwks.decode(token);
                } else {
                    throw new RuntimeException("Unsupported algorithm or missing decoder for token");
                }
            } catch (Exception e) {
                log.error("❌ [Security] JWT Decoding failed: {}", e.getMessage());
                throw new org.springframework.security.oauth2.jwt.JwtException("Invalid token", e);
            }
        };

        // 4. Common Validators
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

        // Apply validators to both underlying decoders if possible, or handle in hybrid
        if (finalJwks != null)
            finalJwks.setJwtValidator(audienceValidator);
        if (finalSecret != null)
            finalSecret.setJwtValidator(audienceValidator);

        return hybridDecoder;
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
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint()))
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/api/v1/auth/**", "/error").permitAll()
                            .requestMatchers("/", "/index.html", "/favicon.ico", "/manifest.json", "/robots.txt",
                                    "/site.webmanifest")
                            .permitAll()
                            .requestMatchers("/static/**", "/assets/**", "/_app/**", "/_vercel/**").permitAll()
                            .requestMatchers("/dashboard", "/dashboard/**").permitAll()
                            .requestMatchers("/api/v1/pdf/**", "/api/v1/cards/**", "/api/v1/waitlist/**").permitAll();

                    if (isSwaggerEnabled) {
                        log.info("✅ Swagger/OpenAPI is ENABLED. Permitting access to /v3/api-docs");
                        auth.requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                .permitAll();
                    } else {
                        log.info("❌ Swagger/OpenAPI is DISABLED.");
                    }

                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())))
                .addFilterAfter(supabaseUserSyncFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            if (isSwaggerEnabled) {
                web.ignoring().requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");
            }
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
