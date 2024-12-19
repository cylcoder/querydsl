package study.practice.dto;

import lombok.Builder;
import study.practice.model.Order;

import java.time.LocalDate;

@Builder
public record OrderResponse(
        Long id,
        LocalDate orderDate,
        Long quantity,
        Long totalPrice,
        String status) {

    public OrderResponse of(Long id, LocalDate orderDate, Long quantity, Long totalPrice, String status) {
        return OrderResponse.builder()
                .id(id)
                .orderDate(orderDate)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .status(status)
                .build();
    }

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .build();
    }

}
