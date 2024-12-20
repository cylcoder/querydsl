package study.practice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.practice.dto.OrderProduct;
import study.practice.dto.OrderStatusSummary;
import study.practice.dto.OrderSummary;
import study.practice.dto.ProductOrderCount;
import study.practice.model.Product;
import study.practice.repository.PracticeRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final PracticeRepository practiceRepository;

    public List<Product> getProducts(Integer minPrice, Integer page, Integer size) {
        return practiceRepository.getProducts(minPrice, (page - 1) * size, size);
    }

    public OrderSummary getOrderSummary(Long productId, String status) {
        return practiceRepository.getOrderSummary(productId, status);
    }

    public List<Product> getProductPriceRangeIn(Long minPrice, Long maxPrice) {
        return practiceRepository.getProductPriceRangeIn(minPrice, maxPrice);
    }

    public Map<String, OrderStatusSummary> getOrderStatisticsByStatus() {
        return practiceRepository.getOrderStatisticsByStatus();
    }

    public OrderProduct getOrderProducts(Long productId, Long page, Long size) {
        return practiceRepository.getOrderProducts(productId, (page - 1) * size, size);
    }

    public ProductOrderCount getProductOrderCount(Long productId) {
        return practiceRepository.getProductOrderCount(productId);
    }

}
