package study.practice.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import study.practice.dto.*;
import study.practice.model.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static study.practice.model.QOrder.order;
import static study.practice.model.QProduct.product;

@Repository
public class PracticeRepository {

    private final EntityManager em;
    private final JPAQueryFactory factory;

    public PracticeRepository(EntityManager em) {
        this.em = em;
        this.factory = new JPAQueryFactory(em);
    }

    public List<Product> getProducts(Integer minPrice, Integer offset, Integer limit) {
        return factory
                .selectFrom(product)
                .where(minPrice == null ? null : product.price.goe(minPrice))
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public OrderSummary getOrderSummary(Long productId, String status) {
        Tuple tuple = factory
                .select(
                        order.quantity.sum(),
                        order.totalPrice.sum()
                )
                .from(order)
                .where(
                        product.id.eq(productId),
                        status == null ? null : order.status.eq(status)
                )
                .fetchOne();

        if (tuple == null) {
            return null;
        }

        Long quantity = tuple.get(order.quantity.sum());
        Long totalPrice = tuple.get(order.totalPrice.sum());
        return OrderSummary.of(productId, quantity, totalPrice);
    }

    public List<Product> getProductPriceRangeIn(Long minPrice, Long maxPrice) {
        return factory
                .selectFrom(product)
                .where(product.price.between(minPrice, maxPrice))
                .fetch();
    }

    public Map<String, OrderStatusSummary> getOrderStatisticsByStatus() {
        Map<String, OrderStatusSummary> map = new HashMap<>();

        List<Tuple> tuples = factory
                .select(
                        order.status,
                        order.count(),
                        order.totalPrice.sum()
                )
                .from(order)
                .groupBy(order.status)
                .orderBy(order.status.asc())
                .fetch();

        for (Tuple tuple : tuples) {
            String status = tuple.get(order.status);
            Long orderCount = tuple.get(order.count());
            Long totalAmount = tuple.get(order.totalPrice.sum());
            OrderStatusSummary orderStatusSummary = OrderStatusSummary.of(orderCount, totalAmount);
            map.put(status, orderStatusSummary);
        }

        return map;
    }

    public OrderProduct getOrderProducts(Long productId, long offset, Long limit) {
        List<OrderResponse> orderResponses = factory
                .selectFrom(order)
                .where(order.product.id.eq(productId))
                .offset(offset)
                .limit(limit)
                .fetch()
                .stream()
                .map(OrderResponse::from)
                .toList();

        return OrderProduct.of(productId, orderResponses);
    }

    public ProductOrderCount getProductOrderCount(Long productId) {
        return factory
                .select(new QProductOrderCount(product.id, product.name, order.count()))
                .from(order)
                .join(order.product, product)
                .groupBy(product.id)
                .having(product.id.eq(productId))
                .fetchOne();
    }

}
