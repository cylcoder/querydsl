package com.example.querydsl.repository;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.*;
import static org.springframework.util.StringUtils.hasText;

import com.example.querydsl.dto.MemberDto;
import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.dto.QMemberTeamDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QTeam;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

  private final EntityManager entityManager;
  private final JPQLQueryFactory queryFactory;

  public void save(Member member) {
    entityManager.persist(member);
  }

  public Optional<Member> findById(Long id) {
    Member member = entityManager.find(Member.class, id);
    return Optional.ofNullable(member);
  }

  public List<Member> findAll() {
    return entityManager
        .createQuery("select m from Member m", Member.class)
        .getResultList();
  }

  public List<Member> findAllByQuerydsl() {
    return queryFactory.selectFrom(member).fetch();
  }

  public List<Member> findByUsername(String username) {
    return entityManager
        .createQuery("select m from Member m where m.username = :username", Member.class)
        .setParameter("username", username)
        .getResultList();
  }

  public List<Member> findByUsernameByQuerydsl(String username) {
    return queryFactory
        .selectFrom(member)
        .where(member.username.eq(username))
        .fetch();
  }

  public List<MemberTeamDto> searchByBuilder(MemberSearchCondition memberSearchCondition) {
    BooleanBuilder booleanBuilder = new BooleanBuilder();

    if (hasText(memberSearchCondition.getUsername())) {
      booleanBuilder.and(member.username.eq(memberSearchCondition.getUsername()));
    }

    if (hasText(memberSearchCondition.getTeamName())) {
      booleanBuilder.and(team.name.eq(memberSearchCondition.getTeamName()));
    }

    if (memberSearchCondition.getAgeLoe() != null) {
      booleanBuilder.and(member.age.loe(memberSearchCondition.getAgeLoe()));
    }

    if (memberSearchCondition.getAgeGoe() != null) {
      booleanBuilder.and(member.age.goe(memberSearchCondition.getAgeGoe()));
    }

    return queryFactory
        .select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
        .from(member)
        .leftJoin(member.team, team)
        .where(booleanBuilder)
        .fetch();
  }

  public List<MemberTeamDto> searchByWhere(MemberSearchCondition memberSearchCondition) {
    return queryFactory
        .select(new QMemberTeamDto(
            member.id,
            member.username,
            member.age,
            team.id,
            team.name
        ))
        .from(member)
        .leftJoin(member.team, team)
        .where(
            usernameEq(memberSearchCondition.getUsername()),
            teamNameEq(memberSearchCondition.getTeamName()),
            ageBetween(memberSearchCondition.getAgeGoe(), memberSearchCondition.getAgeLoe())
        )
        .fetch();
  }

  private BooleanExpression usernameEq(String username) {
    return hasText(username) ? member.username.eq(username) : null;
  }

  private BooleanExpression teamNameEq(String teamName) {
    return hasText(teamName) ? team.name.eq(teamName) : null;
  }

  private BooleanExpression ageLoe(Integer age) {
    return age != null ? member.age.loe(age) : null;
  }

  private BooleanExpression ageGoe(Integer age) {
    return age != null ? member.age.goe(age) : null;
  }

  private BooleanExpression ageBetween(Integer goe, Integer loe) {
    return member.age.goe(goe).and(member.age.loe(loe));
  }

}
