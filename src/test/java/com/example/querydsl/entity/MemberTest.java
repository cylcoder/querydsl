package com.example.querydsl.entity;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Commit
class MemberTest {

  @PersistenceContext
  EntityManager entityManager;

  @Test
  void testEntity() {
    Team foo = new Team("foo");
    Team bar = new Team("bar");
    entityManager.persist(foo);
    entityManager.persist(bar);

    Member baz = new Member("baz", 10, foo);
    Member qux = new Member("qux", 10, foo);
    Member quux = new Member("quux", 10, bar);
    Member corge = new Member("corge", 10, bar);
    entityManager.persist(baz);
    entityManager.persist(qux);
    entityManager.persist(quux);
    entityManager.persist(corge);

    entityManager.flush();
    entityManager.clear();

    List<Member> members = entityManager
        .createQuery("select m from Member m", Member.class)
        .getResultList();

    members.forEach(System.out::println);
  }

}