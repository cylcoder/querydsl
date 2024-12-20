package study.practice.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class ProductOrderCount {

    private final Long productId;

    private final String productName;

    private final Long orderCount;

    @QueryProjection
    public ProductOrderCount(Long productId, String productName, Long orderCount) {
        this.productId = productId;
        this.productName = productName;
        this.orderCount = orderCount;
    }

}
