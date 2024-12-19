package study.practice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long price;

    private String category;

    @OneToMany(mappedBy = "product")
    @JsonBackReference
    private List<Order> orders;

    @Builder
    public Product(String name, Long price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public static Product of(String name, Long price, String category) {
        return Product.builder()
                .name(name)
                .price(price)
                .category(category)
                .build();
    }

}
