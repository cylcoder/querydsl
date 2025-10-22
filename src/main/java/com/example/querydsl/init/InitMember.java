package com.example.querydsl.init;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.Team;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

  private final ItemMemberService itemMemberService;

  @PostConstruct
  public void init() {
    itemMemberService.init();
  }

  @PreDestroy
  public void cleanUp() {
    itemMemberService.cleanUp();
  }

  @Component
  static class ItemMemberService {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void init() {
      Team foo = new Team("foo");
      Team bar = new Team("bar");
      entityManager.persist(foo);
      entityManager.persist(bar);

      for (int i = 0; i < 100; i++) {
        Team team = (i % 2 == 0 ? foo : bar);
        entityManager.persist(new Member("baz" + i, i, team));
      }
    }

    public void cleanUp() {
      entityManager
          .createQuery("delete from Member")
          .executeUpdate();

      entityManager
          .createQuery("delete from Team")
          .executeUpdate();
    }
  }

}
