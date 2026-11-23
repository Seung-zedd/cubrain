package com.cubrain.springboot_starter_auth.global.config.web;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pre-auth JWT length limits: oversized input must be rejected before any Base64 decoding.
 * Limits: whole token 4096 chars, header segment 512 chars.
 */
class SecurityConfigJwtLengthTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long!!";
    private static final int MAX_TOKEN_LENGTH = 4096;
    private static final int MAX_HEADER_LENGTH = 512;

    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        SecurityConfig config = new SecurityConfig(null, null);
        ReflectionTestUtils.setField(config, "issuerUri", "");
        ReflectionTestUtils.setField(config, "projectId", "");
        ReflectionTestUtils.setField(config, "jwtSecret", SECRET);
        decoder = config.jwtDecoder();
    }

    @Test
    void validHs256TokenStillPasses() throws Exception {
        Jwt jwt = decoder.decode(signedHs256Token());

        assertNotNull(jwt);
        assertEquals("user-1", jwt.getSubject());
    }

    @Test
    void tokenLongerThan4096CharsIsRejectedAsTooLarge() {
        String token = "a".repeat(5000);

        JwtException ex = assertThrows(JwtException.class, () -> decoder.decode(token));

        assertTrue(rootMessage(ex).contains("Token too large"), rootMessage(ex));
    }

    @Test
    void tokenOfExactly4097CharsIsRejectedAsTooLarge() {
        String token = tokenOfLength("A".repeat(MAX_HEADER_LENGTH), MAX_TOKEN_LENGTH + 1);

        JwtException ex = assertThrows(JwtException.class, () -> decoder.decode(token));

        assertTrue(rootMessage(ex).contains("Token too large"), rootMessage(ex));
    }

    @Test
    void headerOf513CharsIsRejectedAsHeaderTooLarge() {
        String token = tokenOfLength("A".repeat(MAX_HEADER_LENGTH + 1), 600);

        JwtException ex = assertThrows(JwtException.class, () -> decoder.decode(token));

        assertTrue(rootMessage(ex).contains("Token header too large"), rootMessage(ex));
    }

    @Test
    void headerOf512CharsIsNotRejectedAsTooLarge() {
        String token = tokenOfLength("A".repeat(MAX_HEADER_LENGTH), 600);

        assertNotRejectedAsTooLarge(token);
    }

    @Test
    void tokenOfExactly4096CharsIsNotRejectedAsTooLarge() {
        String token = tokenOfLength("A".repeat(MAX_HEADER_LENGTH), MAX_TOKEN_LENGTH);
        assertEquals(MAX_TOKEN_LENGTH, token.length());

        assertNotRejectedAsTooLarge(token);
    }

    // ---- helpers ----

    /** Boundary tokens are garbage, so they still fail later - just not with a "too large" reason. */
    private void assertNotRejectedAsTooLarge(String token) {
        try {
            decoder.decode(token);
        } catch (JwtException ex) {
            assertTrue(!rootMessage(ex).contains("too large"), rootMessage(ex));
        }
    }

    private static String tokenOfLength(String header, int totalLength) {
        String prefix = header + ".";
        int bodyLength = totalLength - prefix.length();
        return prefix + "B".repeat(bodyLength);
    }

    private static String signedHs256Token() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("user-1")
                .audience(List.of("authenticated"))
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(SECRET.getBytes()));
        return jwt.serialize();
    }

    private static String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        return String.valueOf(cur.getMessage());
    }
}
