package com.cubrain.springboot_starter_auth.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
// BoilerPlate Class
public class EnvironmentUtil {

    private final Environment environment;

    public boolean isLocalEnvironment() {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        return activeProfiles.contains("local") || activeProfiles.contains("dev");
    }

    public boolean isHttpEnvironment() {
        // Local: HTTP, Dev, Prod: HTTPS
        return Arrays.asList(environment.getActiveProfiles()).contains("local");
    }

}
