package com.cubrain.springboot_starter_auth.domain.card;

import io.swagger.v3.oas.annotations.media.Schema;

public record CardRequestDto(
        @Schema(description = "Selected text from the document", example = "TCP is a connection-oriented protocol...") String selection,

        @Schema(description = "Surrounding context of the selection", example = "The Transmission Control Protocol (TCP) is one of the main protocols of the Internet protocol suite.") String localContext,

        @Schema(description = "Global context or document summary", example = "This document explains the basics of networking protocols.") String globalContext) {
}
