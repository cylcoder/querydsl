package com.example.querydsl.repository;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.dto.QMemberTeamDto;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

  private final JPQLQueryFactory queryFactory;

  @Override
  public List<MemberTeamDto> search(MemberSearchCondition memberSearchCondition) {
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
            ageGoe(memberSearchCondition.getAgeGoe()),
            ageLoe(memberSearchCondition.getAgeLoe())
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

  @Override
  public Page<MemberTeamDto> searchPageSimple(
      MemberSearchCondition memberSearchCondition,
      Pageable pageable
  ) {
    QueryResults<MemberTeamDto> queryResults = queryFactory
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
            ageGoe(memberSearchCondition.getAgeGoe()),
            ageLoe(memberSearchCondition.getAgeLoe())
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    List<MemberTeamDto> content = queryResults.getResults();
    long total = queryResults.getTotal();
    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Page<MemberTeamDto> searchPageComplex(
      MemberSearchCondition memberSearchCondition,
      Pageable pageable
  ) {
    List<MemberTeamDto> content = queryFactory
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
            ageGoe(memberSearchCondition.getAgeGoe()),
            ageLoe(memberSearchCondition.getAgeLoe())
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = queryFactory
        .select(member.count())
        .from(member)
        .leftJoin(member.team, team)
        .where(
            usernameEq(memberSearchCondition.getUsername()),
            teamNameEq(memberSearchCondition.getTeamName()),
            ageGoe(memberSearchCondition.getAgeGoe()),
            ageLoe(memberSearchCondition.getAgeLoe())
        )
        .fetchOne();

    return new PageImpl<>(content, pageable, total);
  }

}
