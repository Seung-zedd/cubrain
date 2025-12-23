package com.cubrain.springboot_starter_auth.global.config.web;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class RootController {

    @Value("${springdoc.api-docs.enabled:false}")
    private boolean isSwaggerEnabled;

    @Operation(summary = "Health Check", description = "Returns the current status of the API service.")
    @GetMapping("/")
    public RootResponseDto index() {
        return RootResponseDto.of(
                "UP",
                "Cubrain API Service is running",
                isSwaggerEnabled ? "/swagger-ui.html" : null);
    }

    @Operation(hidden = true)
    @GetMapping("/favicon.ico")
    public void favicon() {
        // No-op to prevent 404 logs for favicon
    }

    @Operation(summary = "API Discovery Redirect", description = "Redirects to OpenAPI documentation if enabled.")
    @GetMapping({ "/swagger.json", "/config.json" })
    public void redirectToApiDocs(HttpServletResponse response) throws IOException {
        if (isSwaggerEnabled) {
            response.sendRedirect("/v3/api-docs");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
