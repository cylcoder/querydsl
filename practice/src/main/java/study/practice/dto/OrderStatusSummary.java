package study.practice.dto;

import lombok.Builder;

@Builder
public record OrderStatusSummary(Long orderCount, Long totalAmount) {

    public static OrderStatusSummary of(Long orderCount, Long totalAmount) {
        return OrderStatusSummary.builder()
                .orderCount(orderCount)
                .totalAmount(totalAmount)
                .build();
    }

}
