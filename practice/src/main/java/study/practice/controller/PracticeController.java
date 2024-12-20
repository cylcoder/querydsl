package study.practice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import study.practice.dto.OrderProduct;
import study.practice.dto.OrderStatusSummary;
import study.practice.dto.OrderSummary;
import study.practice.dto.ProductOrderCount;
import study.practice.model.Product;
import study.practice.service.PracticeService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class PracticeController {

    private final PracticeService practiceService;

    @GetMapping("/products")
    public List<Product> getProducts(
            @RequestParam(required = false, name = "min_price") Integer minPrice,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size
            ){
        return practiceService.getProducts(minPrice, page, size);
    }

    @GetMapping("/orders/{productId}/summary")
    public OrderSummary getOrderSummary(
            @PathVariable Long productId,
            @RequestParam(required = false) String status) {
        return practiceService.getOrderSummary(productId, status);
    }

    @GetMapping("/products/price-range")
    public List<Product> getProductPriceRangeIn(
            @RequestParam(name = "min_price") Long minPrice,
            @RequestParam(name = "max_price") Long maxPrice) {
        return practiceService.getProductPriceRangeIn(minPrice, maxPrice);
    }

    @GetMapping("/orders/status-summary")
    public Map<String, OrderStatusSummary> getOrderStatisticsByStatus() {
        return practiceService.getOrderStatisticsByStatus();
    }

    @GetMapping("/orders/products")
    public OrderProduct getOrderProducts(
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        return practiceService.getOrderProducts(productId, page, size);
    }

    @GetMapping("/products/{productId}/order-count")
    public ProductOrderCount getProductOrderCount(@PathVariable Long productId) {
        return practiceService.getProductOrderCount(productId);
    }

}
