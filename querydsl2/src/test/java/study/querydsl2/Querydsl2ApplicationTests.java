package study.querydsl2;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl2.entity.Hello;
import study.querydsl2.entity.QHello;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Commit
class Querydsl2ApplicationTests {

    @Autowired
    EntityManager entityManager;

    @Test
    void contextLoads() {
        Hello hello = new Hello();
        entityManager.persist(hello);
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(entityManager);
        QHello qHello = QHello.hello;

        Hello fetchedOne = jpaQueryFactory
                .selectFrom(qHello)
                .fetchOne();

        assertThat(fetchedOne).isNotNull().isEqualTo(hello);
        assertThat(fetchedOne.getId()).isEqualTo(hello.getId());
    }

}
