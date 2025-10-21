package com.example.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

  @PersistenceContext
  EntityManager entityManager;

  @Autowired
  MemberJpaRepository memberJpaRepository;

  @Test
  void basicTest() {
    Member foo = new Member("foo", 10, null);
    memberJpaRepository.save(foo);

    Member foundFoo1 = memberJpaRepository.findById(foo.getId()).get();
    assertThat(foundFoo1).isEqualTo(foo);

    List<Member> members = memberJpaRepository.findAll();
    assertThat(members).containsExactly(foo);

    List<Member> foundFoo2 = memberJpaRepository.findByUsername(foo.getUsername());
    assertThat(foundFoo2).containsExactly(foo);
  }

  @Test
  void basicQuerydslTest() {
    Member foo = new Member("foo", 10, null);
    memberJpaRepository.save(foo);

    Member foundFoo1 = memberJpaRepository.findById(foo.getId()).get();
    assertThat(foundFoo1).isEqualTo(foo);

    List<Member> members = memberJpaRepository.findAllByQuerydsl();
    assertThat(members).containsExactly(foo);

    List<Member> foundFoo2 = memberJpaRepository.findByUsernameByQuerydsl(foo.getUsername());
    assertThat(foundFoo2).containsExactly(foo);
  }

  @Test
  void searchTest() {
    Team foo = new Team("foo");
    Team bar = new Team("bar");
    entityManager.persist(foo);
    entityManager.persist(bar);

    Member baz = new Member("baz", 10, foo);
    Member qux = new Member("qux", 20, foo);
    Member quux = new Member("quux", 30, bar);
    Member corge = new Member("corge", 40, bar);
    entityManager.persist(baz);
    entityManager.persist(qux);
    entityManager.persist(quux);
    entityManager.persist(corge);

    MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
    memberSearchCondition.setAgeLoe(40);
    memberSearchCondition.setAgeGoe(35);
    memberSearchCondition.setTeamName("bar");

    List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByWhere(memberSearchCondition);
    assertThat(memberTeamDtos)
        .extracting("username")
        .containsExactly("corge");
  }

}