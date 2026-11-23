package com.cubrain.springboot_starter_auth.global.config.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.env.MockEnvironment;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

    @Test
    void defaultOriginsContainNoWildcardAndIncludeProductionFrontend() throws Exception {
        List<String> origins = resolveDefaultOrigins();

        assertTrue(origins.stream().noneMatch(o -> o.contains("*")), "wildcard origin found: " + origins);
        assertTrue(origins.contains("https://cubrain.vercel.app"), origins.toString());
        assertTrue(origins.contains("http://localhost:5173"), origins.toString());
    }

    @Test
    void defaultOriginsDoNotAllowAllVercelApps() throws Exception {
        assertFalse(resolveDefaultOrigins().contains("https://*.vercel.app"));
    }

    private List<String> resolveDefaultOrigins() throws Exception {
        Value value = CorsConfig.class.getDeclaredField("allowedOrigins").getAnnotation(Value.class);
        String resolved = new MockEnvironment().resolveRequiredPlaceholders(value.value());
        return Arrays.stream(resolved.split(",")).map(String::trim).toList();
    }
}
