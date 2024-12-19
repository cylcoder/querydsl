package study.practice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate orderDate;

    private Long quantity;

    private Long totalPrice;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @Builder
    public Order(LocalDate orderDate, Long quantity, Long totalPrice, String status, Product product) {
        this.orderDate = orderDate;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
        this.product = product;
    }

    public static Order of(LocalDate orderDate, Long quantity, Long totalPrice, String status, Product product) {
        return Order.builder()
                .orderDate(orderDate)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .status(status)
                .product(product)
                .build();
    }

}
