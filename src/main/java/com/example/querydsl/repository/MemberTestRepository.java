package com.example.querydsl.repository;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.entity.Member;
import com.example.querydsl.repository.support.Querydsl4RepositorySupport;
import com.querydsl.core.QueryFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {

  private final QueryFactory queryFactory;

  public MemberTestRepository(QueryFactory queryFactory) {
    super(Member.class);
    this.queryFactory = queryFactory;
  }

  public List<Member> basicSelect() {
    return select(member)
        .from(member)
        .fetch();
  }

  public List<Member> basicSelectFrom() {
    return selectFrom(member).fetch();
  }

  public Page<Member> searchPageByApplyPage(
      MemberSearchCondition memberSearchCondition,
      Pageable pageable
  ) {
    JPAQuery<Member> query = selectFrom(member)
        .leftJoin(member.team, team)
        .where(
            usernameEq(memberSearchCondition.getUsername()),
            teamNameEq(memberSearchCondition.getTeamName()),
            ageGoe(memberSearchCondition.getAgeGoe()),
            ageLoe(memberSearchCondition.getAgeLoe())
        );

    List<Member> content = getQuerydsl()
        .applyPagination(pageable, query)
        .fetch();

    return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
  }

  public Page<Member> applyPagination(
      MemberSearchCondition memberSearchCondition,
      Pageable pageable
  ) {
    return applyPagination(pageable, contentQuery -> contentQuery
        .selectFrom(member)
        .leftJoin(member.team, team)
        .where(
            usernameEq(memberSearchCondition.getUsername()),
            teamNameEq(memberSearchCondition.getTeamName()),
            ageGoe(memberSearchCondition.getAgeGoe()),
            ageLoe(memberSearchCondition.getAgeLoe())
        ));
  }

  public Page<Member> applyPagination2(
      MemberSearchCondition memberSearchCondition,
      Pageable pageable
  ) {
    return applyPagination(
        pageable,
        contentQuery -> contentQuery
            .selectFrom(member)
            .leftJoin(member.team, team)
            .where(usernameEq(memberSearchCondition.getUsername()),
                teamNameEq(memberSearchCondition.getTeamName()),
                ageGoe(memberSearchCondition.getAgeGoe()),
                ageLoe(memberSearchCondition.getAgeLoe())),
        countQuery -> countQuery
            .selectFrom(member)
            .leftJoin(member.team, team)
            .where(usernameEq(memberSearchCondition.getUsername()),
                teamNameEq(memberSearchCondition.getTeamName()),
                ageGoe(memberSearchCondition.getAgeGoe()),
                ageLoe(memberSearchCondition.getAgeLoe()))
    );
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

}
