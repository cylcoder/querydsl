package com.example.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

  @PersistenceContext
  EntityManager entityManager;

  @Autowired
  MemberRepository memberRepository;

  @Test
  void basicTest() {
    Member member = new Member("foo", 10, null);
    memberRepository.save(member);

    Member foundMember = memberRepository.findById(member.getId()).get();
    assertThat(foundMember).isEqualTo(member);

    List<Member> members1 = memberRepository.findAll();
    assertThat(members1).containsExactly(member);

    List<Member> members2 = memberRepository.findByUsername(member.getUsername());
    assertThat(members2).containsExactly(member);
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

    List<MemberTeamDto> memberTeamDtos = memberRepository.search(memberSearchCondition);
    assertThat(memberTeamDtos)
        .extracting("username")
        .containsExactly("corge");
  }

  @Test
  void searchPageSimple() {
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
    PageRequest pageRequest = PageRequest.of(0, 3);
    Page<MemberTeamDto> page = memberRepository
        .searchPageSimple(memberSearchCondition, pageRequest);

    assertThat(page).hasSize(3);
    assertThat(page.getContent())
        .extracting("username")
        .containsExactly("baz", "qux", "quux");
  }

}
