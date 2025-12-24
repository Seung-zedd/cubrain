package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import com.cubrain.springboot_starter_auth.domain.card.v1.CardService;
import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.cubrain.springboot_starter_auth.global.usage.UsageLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PdfIngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PdfAnnotationService pdfAnnotationService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private UsageLimitService usageLimitService;

    @MockBean
    private CardService cardService;

    @MockBean
    private JobManager jobManager;

    @BeforeEach
    void setUp() {
        Member freeMember = Member.builder()
                .email("user@example.com")
                .tier(UserTier.FREE_USER)
                .build();
        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(freeMember));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void generateCards_PageLimitExceeded_FreeUser() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf",
                "test content".getBytes());
        when(pdfAnnotationService.getPageCount(any())).thenReturn(51);

        // When & Then
        mockMvc.perform(multipart("/api/v1/pdf/generate-cards").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Page limit exceeded. FREE_USER limit is 50 pages."));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void generateCards_WithinPageLimit_FreeUser() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf",
                "test content".getBytes());
        when(pdfAnnotationService.getPageCount(any())).thenReturn(50);
        when(cardService.generateCardsAsync(any(), any(), any())).thenReturn("job-123");

        // When & Then
        mockMvc.perform(multipart("/api/v1/pdf/generate-cards").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-123"));
    }
}
