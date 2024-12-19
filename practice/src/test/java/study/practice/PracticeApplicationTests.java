package study.practice;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.practice.model.Order;
import study.practice.model.Product;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@Transactional
@Commit
class PracticeApplicationTests {

    @Autowired
    @PersistenceContext
    EntityManager em;

    JPAQueryFactory factory;

    @BeforeEach
    void beforeEach() {
        factory = new JPAQueryFactory(em);
    }

    @Test
    void contextLoads() {
        // 1. Products for different categories
        for (int i = 0; i < 20; i++) {
            em.persist(Product.of("Electronics " + i, (long) (5000 + (i * 100)), "Electronics"));
        }
        for (int i = 0; i < 20; i++) {
            em.persist(Product.of("Books " + i, (long) (1000 + (i * 50)), "Books"));
        }
        for (int i = 0; i < 20; i++) {
            em.persist(Product.of("Furniture " + i, (long) (20000 + (i * 500)), "Furniture"));
        }
        for (int i = 0; i < 20; i++) {
            em.persist(Product.of("Clothing " + i, (long) (3000 + (i * 200)), "Clothing"));
        }
        for (int i = 0; i < 20; i++) {
            em.persist(Product.of("Groceries " + i, (long) (500 + (i * 10)), "Groceries"));
        }

        // 2. Fetch all products for order creation
        List<Product> products = em.createQuery("select p from Product p", Product.class).getResultList();

        // 3. Orders with varying dates and quantities
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            for (int j = 1; j <= 5; j++) { // Each product will have 5 orders
                em.persist(Order.of(
                        LocalDate.now().minusDays(i + j), // Varying order dates
                        (long) (j * 2), // Quantity
                        (long) (product.getPrice() * (j * 2)), // Total price based on quantity
                        j % 3 == 0 ? "SHIPPED" : j % 2 == 0 ? "PROCESSING" : "CANCELLED", // Status
                        product
                ));
            }
        }
    }

}
