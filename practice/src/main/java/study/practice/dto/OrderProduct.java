package study.practice.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderProduct(Long productId, List<OrderResponse> orderResponses) {

    public static OrderProduct of(Long productId, List<OrderResponse> orderResponses) {
        return OrderProduct.builder()
                .productId(productId)
                .orderResponses(orderResponses)
                .build();
    }

}
