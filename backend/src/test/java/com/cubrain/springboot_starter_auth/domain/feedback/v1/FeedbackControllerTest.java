package com.cubrain.springboot_starter_auth.domain.feedback.v1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedbackService feedbackService;

    @Test
    @DisplayName("Authenticated user can submit feedback")
    void submitFeedback_Authenticated_ShouldSucceed() throws Exception {
        String json = """
                {
                    "goals": ["Speed Reading"],
                    "painPoints": ["Focus Villain"],
                    "shortFeedback": "Great app!"
                }
                """;

        mockMvc.perform(post("/api/v1/feedback")
                .with(jwt().jwt(jwt -> jwt.claim("email", "test@example.com").subject("user-123")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(feedbackService).sendFeedbackEmail(any(FeedbackRequestDto.class), anyString());
    }

    @Test
    @DisplayName("Unauthenticated user cannot submit feedback")
    void submitFeedback_Unauthenticated_ShouldReturn401() throws Exception {
        String json = """
                {
                    "goals": ["Speed Reading"],
                    "painPoints": ["Focus Villain"],
                    "shortFeedback": "Great app!"
                }
                """;

        mockMvc.perform(post("/api/v1/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }
}
