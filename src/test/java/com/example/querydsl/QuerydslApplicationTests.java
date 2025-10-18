package com.example.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Commit
class QuerydslApplicationTests {

  @PersistenceContext
  EntityManager entityManager;

  @Test
  void save() {
    Demo demo = new Demo();
    entityManager.persist(demo);
    JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
    QDemo qDemo = QDemo.demo;
    Demo foundDemo = queryFactory
        .selectFrom(qDemo)
        .fetchOne();

    assertThat(foundDemo).isEqualTo(demo);
  }

  @Test
  void contextLoads() {
  }

}
