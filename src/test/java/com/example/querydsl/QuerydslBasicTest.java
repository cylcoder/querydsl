package com.example.querydsl;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.team;
import static com.querydsl.core.types.Projections.bean;
import static com.querydsl.core.types.Projections.constructor;
import static com.querydsl.core.types.Projections.fields;
import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.querydsl.dto.MemberDto;
import com.example.querydsl.dto.QMemberDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class QuerydslBasicTest {

  @PersistenceUnit
  EntityManagerFactory entityManagerFactory;

  @PersistenceContext
  EntityManager entityManager;

  JPAQueryFactory queryFactory;

  @BeforeEach
  void setUp() {
    queryFactory = new JPAQueryFactory(entityManager);

    Team foo = new Team("foo");
    Team bar = new Team("bar");
    entityManager.persist(foo);
    entityManager.persist(bar);

    Member baz = new Member("baz", 20, foo);
    Member qux = new Member("qux", 20, foo);
    Member quux = new Member("quux", 30, bar);
    Member corge = new Member("corge", 40, bar);

    entityManager.persist(baz);
    entityManager.persist(qux);
    entityManager.persist(quux);
    entityManager.persist(corge);
  }

  @Test
  void startJPQL() {
    // Find baz.
    String jpql = "select m from Member m where m.username = :username";
    Member baz = entityManager.createQuery(jpql, Member.class)
        .setParameter("username", "baz")
        .getSingleResult();

    assertThat(baz.getUsername()).isEqualTo("baz");
  }

  @Test
  void startQuerydsl() {
    Member baz = queryFactory
        .select(member)
        .from(member)
        .where(member.username.eq("baz"))
        .fetchOne();

    assertThat(baz.getUsername()).isEqualTo("baz");
  }

  @Test
  void search() {
    Member baz = queryFactory
        .selectFrom(member)
        .where(
            member.username.eq("baz")
                .and(member.age.eq(20))
        )
        .fetchOne();

    assertThat(baz.getUsername()).isEqualTo("baz");
  }

  @Test
  void searchAndParam() {
    List<Member> members = queryFactory
        .selectFrom(member)
        .where(member.username.eq("baz"), member.age.eq(20))
        .fetch();

    assertThat(members).hasSize(1);
  }

  @Test
  void resultFetch() {
    List<Member> fetch = queryFactory
        .selectFrom(member)
        .fetch();

    Member fetchOne = queryFactory
        .selectFrom(member)
        .where(member.username.eq("baz"))
        .fetchOne();

    Member fetchFirst = queryFactory
        .selectFrom(member)
        .fetchFirst();

    QueryResults<Member> fetchResults = queryFactory
        .selectFrom(member)
        .fetchResults();

    long total = fetchResults.getTotal();
    List<Member> results = fetchResults.getResults();

    long fetchCount = queryFactory
        .selectFrom(member)
        .fetchCount();
  }

  /*
   * 회원 정렬 순서
   * 1. 회원 나이 내림차순
   * 2. 회원 이름 올림차순
   * 단, 2에서 회원 이름이 없으면 마지막에 출력
   * */
  @Test
  void sort() {
    Member grault = new Member(null, 20, null);
    entityManager.persist(grault);

    List<Member> members = queryFactory
        .selectFrom(member)
        .orderBy(
            member.age.desc(),
            member.username.asc().nullsLast()
        )
        .fetch();

    // corge -> quux -> baz -> qux -> null
    List<String> foo = Arrays.asList("corge", "quux", "baz", "qux", null);
    List<String> bar = members.stream().map(Member::getUsername).toList();
    assertThat(bar).containsExactlyElementsOf(foo);
  }

  @Test
  void paging1() {
    List<Member> result = queryFactory
        .selectFrom(member)
        .orderBy(member.username.desc())
        .offset(1) // 건너뛸 개수
        .limit(2) // 최대 2건 조회
        .fetch();

    assertThat(result).hasSize(2);
  }

  @Test
  void paging2() {
    QueryResults<Member> fetchResults = queryFactory
        .selectFrom(member)
        .orderBy(member.username.desc())
        .offset(1)
        .limit(2)
        .fetchResults();

    assertThat(fetchResults.getTotal()).isEqualTo(4);
    assertThat(fetchResults.getLimit()).isEqualTo(2);
    assertThat(fetchResults.getOffset()).isEqualTo(1);
    assertThat(fetchResults.getResults()).hasSize(2);
  }

  @Test
  void aggregation() {
    Tuple tuple = queryFactory
        .select(
            member.count(),
            member.age.sum(),
            member.age.avg(),
            member.age.max(),
            member.age.min()
        )
        .from(member)
        .fetchOne();

    assertThat(tuple.get(member.count())).isEqualTo(4);
    assertThat(tuple.get(member.age.sum())).isEqualTo(110);
    assertThat(tuple.get(member.age.max())).isEqualTo(40);
    assertThat(tuple.get(member.age.min())).isEqualTo(20);
    assertThat(tuple.get(member.age.avg())).isEqualTo(27.5);
  }

  // 팀의 이름과 각 팀의 평균 연령을 구해라.
  @Test
  void group() {
    List<Tuple> tuples = queryFactory
        .select(team.name, member.age.avg())
        .from(member)
        .join(member.team, team)
        .groupBy(member.team)
        .having(team.name.ne("bar"))
        .fetch();
  }

  // 팀 foo에 소속된 모든 회원
  @Test
  void join() {
    List<Member> members = queryFactory
        .selectFrom(member)
        .join(member.team, team)
        .where(team.name.eq("foo"))
        .fetch();

    assertThat(members)
        .extracting("username")
        .containsExactly("baz", "qux");
  }

  // 회원의 이름이 팀 이름과 같은 회원 조회
  @Test
  void theta_join() {
    entityManager.persist(new Member("foo", 10, null));

    Member member = queryFactory
        .select(QMember.member)
        .from(QMember.member, team)
        .where(QMember.member.username.eq(team.name))
        .fetchOne();

    assertThat(member.getUsername()).isEqualTo("foo");
  }

  // 회원과 팀을 조인하면서 팀 이름이 foo인 팀만 조인, 회원은 모두 조회
  @Test
  void join_on_filtering() {
    List<Tuple> tuples = queryFactory
        .select(team, member)
        .from(member)
        .leftJoin(member.team, team)
        .on(team.name.eq("foo"))
        .fetch();

    for (Tuple tuple : tuples) {
      System.out.println("tuple = " + tuple);
    }
  }

  // 연관관계가 없는 엔티티 외부 조인
  // 회원의 이름과 팀의 이름이 같은 대상 외부 조인
  @Test
  void join_on_no_relation() {
    entityManager.persist(new Member("foo", 0, null));
    entityManager.persist(new Member("foo", 0, null));

    List<Tuple> tuples = queryFactory
        .select(member, team)
        .from(member)
        .leftJoin(team)
        .on(member.username.eq(team.name))
        .fetch();

    for (Tuple tuple : tuples) {
      System.out.println("tuple = " + tuple);
    }
  }

  @Test
  void fetchJoinNo() {
    entityManager.flush();
    entityManager.clear();

    Member member = queryFactory
        .selectFrom(QMember.member)
        .where(QMember.member.username.eq("baz"))
        .fetchOne();

    boolean isLoaded = entityManagerFactory
        .getPersistenceUnitUtil()
        .isLoaded(member.getTeam());

    assertThat(isLoaded).as("페치 조인 미적용").isFalse();
  }

  @Test
  void fetchJoinUse() {
    entityManager.flush();
    entityManager.clear();

    Member member = queryFactory
        .selectFrom(QMember.member)
        .join(QMember.member.team, team)
        .fetchJoin()
        .where(QMember.member.username.eq("baz"))
        .fetchOne();

    boolean isLoaded = entityManagerFactory
        .getPersistenceUnitUtil()
        .isLoaded(member.getTeam());

    assertThat(isLoaded).as("페치 조인 적용").isTrue();
  }

  // 나이가 가장 많은 회원 조회
  @Test
  void subQuery() {
    QMember sub = new QMember("sub");

    Member member = queryFactory
        .selectFrom(QMember.member)
        .where(QMember.member.age.eq(
            select(sub.age.max())
                .from(sub)
        ))
        .fetchOne();

    assertThat(member.getAge()).isEqualTo(40);
  }

  // 나이가 평균 이상인 회원
  void subQueryGoe() {
    QMember sub = new QMember("sub");

    List<Member> members = queryFactory
        .selectFrom(member)
        .where(member.age.goe(
            select(sub.age.avg())
                .from(sub)
        ))
        .fetch();

    assertThat(members)
        .extracting("age")
        .containsExactly(30, 40);
  }

  // in을 사용한 서브쿼리 여러 건 처리
  // 20세 이상인 회원
  @Test
  void subQueryIn() {
    QMember sub = new QMember("sub");

    List<Member> members = queryFactory
        .selectFrom(member)
        .where(member.age.in(
            select(sub.age)
                .from(sub)
                .where(sub.age.goe(20))
        ))
        .fetch();

    assertThat(
        members
            .stream()
            .allMatch(m -> m.getAge() >= 20)
    ).isTrue();
  }

  @Test
  void selectSubQUery() {
    QMember sub = new QMember("sub");

    List<Tuple> tuples = queryFactory
        .select(
            member.username,
            select(sub.age.avg())
                .from(sub)
        )
        .from(member)
        .fetch();

    for (Tuple tuple : tuples) {
      System.out.println("tuple = " + tuple);
    }
  }

  @Test
  void basicCase() {
    List<String> ages = queryFactory
        .select(member.age
            .when(10).then("열살")
            .when(20).then("스무살")
            .otherwise("기타")
        )
        .from(member)
        .fetch();

    System.out.println("ages = " + ages);
  }

  // 아래와 같은 로직들은 가능하면 애플리케이션 레벨 또는 프리젠테이션 레벨에서 작업할 것
  @Test
  void complexCase() {
    List<String> ages = queryFactory
        .select(new CaseBuilder()
            .when(member.age.between(0, 20)).then("0-20살")
            .when(member.age.between(21, 30)).then("21-30살")
            .otherwise("기타")
        )
        .from(member)
        .fetch();

    System.out.println("ages = " + ages);
  }

  @Test
  void constant() {
    List<Tuple> tuples = queryFactory
        .select(member.username, Expressions.constant("A"))
        .from(member)
        .fetch();

    System.out.println("tuples = " + tuples);
  }

  @Test
  void concat() {
    String baz = queryFactory
        .select(
            member.username
            .concat("-")
            .concat(member.age.stringValue())
        )
        .from(member)
        .where(member.username.eq("baz"))
        .fetchOne();

    System.out.println("baz = " + baz);
  }

  @Test
  void simpleProjection() {
    List<String> names = queryFactory
        .select(member.username)
        .from(member)
        .fetch();

    System.out.println("names = " + names);
  }

  @Test
  void tupleProject() {
    List<Tuple> tuples = queryFactory
        .select(member.username, member.age)
        .from(member)
        .fetch();

    for (Tuple tuple : tuples) {
      System.out.println("username = " + tuple.get(member.username));
      System.out.println("age = " + tuple.get(member.age));
    }
  }

  @Test
  void findDtoByJPQL() {
    List<MemberDto> memberDtos = entityManager.createQuery(
        "select new com.example.querydsl.dto.MemberDto(m.username, m.age) from Member m",
        MemberDto.class).getResultList();
    memberDtos.forEach(System.out::println);
  }

  @Test
  void findDtoBySetter() {
    List<MemberDto> memberDtos = queryFactory
        .select(bean(
            MemberDto.class,
            member.username,
            member.age
        ))
        .from(member)
        .fetch();

    memberDtos.forEach(System.out::println);
  }

  @Test
  void findDtoByField() {
    List<MemberDto> memberDtos = queryFactory
        .select(fields(
            MemberDto.class,
            member.username,
            member.age
        ))
        .from(member)
        .fetch();

    memberDtos.forEach(System.out::println);
  }

  @Test
  void findDtoByConstructor() {
    List<MemberDto> memberDtos = queryFactory
        .select(constructor(
            MemberDto.class,
            member.age,
            member.age
        ))
        .from(member)
        .fetch();

    memberDtos.forEach(System.out::println);
  }

  @Test
  void findDtoByQueryProjection() {
    List<MemberDto> memberDtos = queryFactory
        .select(new QMemberDto(member.username, member.age))
        .from(member)
        .fetch();

    System.out.println("memberDtos = " + memberDtos);
  }

  @Test
  void dynamicQueryByBooleanBuilder() {
    String usernameParam = "baz";
    int ageParam = 20;

    List<Member> members = searchMember1(usernameParam, ageParam);
    assertThat(members).hasSize(1);
  }

  private List<Member> searchMember1(String usernameCond, Integer ageCond) {
    BooleanBuilder booleanBuilder = new BooleanBuilder();
    if (usernameCond != null) {
      booleanBuilder.and(member.username.eq(usernameCond));
    }

    if (ageCond != null) {
      booleanBuilder.and(member.age.eq(ageCond));
    }

    return queryFactory
        .selectFrom(member)
        .where(booleanBuilder)
        .fetch();
  }

  @Test
  void dynamicQueryByWhereParam() {
    String usernameParam = "baz";
    int ageParam = 20;

    List<Member> members = searchMember2(usernameParam, ageParam);
    assertThat(members).hasSize(1);
  }

  private List<Member> searchMember2(String usernameCond, Integer ageCond) {
    return queryFactory
        .selectFrom(member)
        .where(usernameEq(usernameCond), ageEq(ageCond))
        .fetch();
  }

  private List<Member> searchMember3(String usernameCond, Integer ageCond) {
    return queryFactory
        .selectFrom(member)
        .where(allEq(usernameCond, ageCond))
        .fetch();
  }

  private BooleanExpression usernameEq(String usernameCond) {
    return usernameCond != null ? member.username.eq(usernameCond) : null;
  }

  private BooleanExpression ageEq(Integer ageCond) {
    return ageCond != null ? member.age.eq(ageCond) : null;
  }

  private BooleanExpression allEq(String usernameCond, Integer ageCond) {
    return usernameEq(usernameCond).and(ageEq(ageCond));
  }

  @Test
  void bulkUpdate() {
    queryFactory
        .update(member)
        .set(member.username, "30세 미만")
        .where(member.age.lt(30))
        .execute();

    // update는 영속성 컨텍스트가 아닌 DB로 직접 쿼리를 날림
    // 영속성 컨텍스트를 비워주지 않으면 수정 전의 데이터가 조회됨
    entityManager.flush();
    entityManager.clear();

    queryFactory
        .selectFrom(member)
        .fetch()
        .forEach(System.out::println);
  }

  @Test
  void bulkAdd() {
    queryFactory
        .update(member)
        .set(member.age, member.age.add(1)) // 곱하기는 multiply()
        .execute();

    entityManager.flush();
    entityManager.clear();

    queryFactory
        .selectFrom(member)
        .fetch()
        .forEach(System.out::println);
  }

  @Test
  void bulkDelete() {
    queryFactory
        .delete(member)
        .where(member.age.loe(20))
        .execute();

    entityManager.flush();
    entityManager.clear();

    queryFactory
        .selectFrom(member)
        .fetch()
        .forEach(System.out::println);
  }

  @Test
  void sqlFunction() {
    String s = queryFactory
        .select(Expressions.stringTemplate(
            "function('replace', {0}, {1}, {2})",
            member.username,
            "z", "s")
        )
        .from(member)
        .fetchFirst();

    System.out.println("s = " + s);
  }

}
