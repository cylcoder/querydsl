package study.practice.dto;

import lombok.Builder;

@Builder
public record OrderSummary(Long productId, Long quantity, Long sum) {

    public static OrderSummary of(Long productId, Long quantity, Long sum) {
        return OrderSummary.builder()
                .productId(productId)
                .quantity(quantity)
                .sum(sum)
                .build();
    }

}
