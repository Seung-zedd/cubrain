package com.cubrain.springboot_starter_auth.global.auth;

import com.cubrain.springboot_starter_auth.global.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Request verification code", description = "Sends a 6-digit verification code to the user's email.")
    @PostMapping("/request-code")
    public ResponseEntity<Void> requestCode(@Valid @RequestBody AuthRequestDto request,
            @RequestParam AuthMode mode) {
        authService.requestVerification(request.email(), mode);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Verify code and authenticate", description = "Verifies the 6-digit code and issues JWT tokens if successful.")
    @PostMapping("/verify")
    public ResponseEntity<TokenResponseDto> verify(@Valid @RequestBody VerifyRequestDto request,
            @RequestParam AuthMode mode,
            HttpServletResponse response) {
        TokenResponseDto tokenResponse = authService.verifyAndAuthenticate(request.email(),
                request.code(),
                mode);
        setTokenCookies(response, tokenResponse);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "Refresh JWT tokens", description = "Issues new access and refresh tokens using a valid refresh token.")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken") String refreshToken,
            HttpServletResponse response) {
        try {
            TokenResponseDto tokenResponse = authService.refreshTokens(refreshToken);
            setTokenCookies(response, tokenResponse);
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get current user info", description = "Returns the profile of the currently authenticated user.")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.getMe(principal.getName()));
    }

    @Operation(summary = "Logout", description = "Clears the authentication cookies.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie accessCookie = jwtTokenProvider.createAccessTokenCookie("");
        ResponseCookie refreshCookie = jwtTokenProvider.createRefreshTokenCookie("");

        // Overwrite with max-age 0
        accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false) // Should match the original setting, but false is safe for dev
                .path("/")
                .maxAge(0)
                .build();

        refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok().build();
    }

    private void setTokenCookies(HttpServletResponse response, TokenResponseDto tokenResponse) {
        ResponseCookie accessCookie = jwtTokenProvider.createAccessTokenCookie(tokenResponse.accessToken());
        ResponseCookie refreshCookie = jwtTokenProvider.createRefreshTokenCookie(tokenResponse.refreshToken());

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }
}
