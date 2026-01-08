package com.cubrain.springboot_starter_auth.global.config.lemonsqueezy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Lemon Squeezy Configuration DTO")
public record LemonSqueezyConfigDto(
        @Schema(description = "Lemon Squeezy Product ID", example = "YOUR_PRODUCT_ID") String productId,

        @Schema(description = "Lemon Squeezy Base Checkout URL", example = "https://yourstore.lemonsqueezy.com/checkout/buy/") String checkoutUrl,

        @Schema(description = "Monthly Variant ID", example = "YOUR_MONTHLY_VARIANT_ID") String monthlyVariantId,

        @Schema(description = "Yearly Variant ID", example = "YOUR_YEARLY_VARIANT_ID") String yearlyVariantId) {
    public static LemonSqueezyConfigDto from(LemonSqueezyProperties props) {
        return LemonSqueezyConfigDto.builder()
                .productId(props.productId())
                .checkoutUrl(props.checkoutUrl())
                .monthlyVariantId(props.variants().monthly())
                .yearlyVariantId(props.variants().yearly())
                .build();
    }
}
