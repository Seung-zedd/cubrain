package com.cubrain.springboot_starter_auth.global.config.lemonsqueezy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Lemon Squeezy Configuration DTO")
public record LemonSqueezyConfigDto(
        @Schema(description = "Lemon Squeezy Product ID", example = "646b9e10-0039-4c37-bb30-2ffa5fa2b32f") String productId,

        @Schema(description = "Lemon Squeezy Base Checkout URL", example = "https://cubrain.lemonsqueezy.com/checkout/buy/") String checkoutUrl,

        @Schema(description = "Monthly Variant ID", example = "646b9e10-0039-4c37-bb30-2ffa5fa2b32f") String monthlyVariantId,

        @Schema(description = "Yearly Variant ID", example = "YOUR_ANNUAL_VARIANT_ID") String yearlyVariantId) {
    public static LemonSqueezyConfigDto from(LemonSqueezyProperties props) {
        return LemonSqueezyConfigDto.builder()
                .productId(props.productId())
                .checkoutUrl(props.checkoutUrl())
                .monthlyVariantId(props.variants().monthly())
                .yearlyVariantId(props.variants().yearly())
                .build();
    }
}
