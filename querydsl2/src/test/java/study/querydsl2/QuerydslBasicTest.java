package study.querydsl2;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl2.entity.Member;
import study.querydsl2.entity.QMember;
import study.querydsl2.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static study.querydsl2.entity.QMember.member;
import static study.querydsl2.entity.QTeam.team;

@SpringBootTest
@Transactional
class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory factory;

    @BeforeEach
    void beforeEach() {
        factory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void startJPQL() {
        // find member1
        String query = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(query, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQuerydsl() {
        Member findMember = factory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember).isNotNull();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void search() {
        Member findMember = factory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember).isNotNull();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void searchAndParam() {
        Member findMember = factory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10))
                .fetchOne();

        assertThat(findMember).isNotNull();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void resultFetch() {
        List<Member> fetch = factory
                .selectFrom(member)
                .fetch();

        Member fetchOne = factory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        Member fetchFirst = factory
                .selectFrom(member)
                .fetchFirst();

        QueryResults<Member> fetchResults = factory
                .selectFrom(member)
                .fetchResults();

        long total = fetchResults.getTotal();
        List<Member> results = fetchResults.getResults();

        long fetchCount = factory
                .selectFrom(member)
                .fetchCount();
    }

    /*
    * 회원 정렬 기준
    * 1. 회원 나이 내림차순
    * 2. 회원 이름 올림차순
    * 3. 회원 이름이 null이라면 마지막에 출력
    * */
    @Test
    void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> members = factory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(
                        member.age.desc(),
                        member.username.asc().nullsLast()
                )
                .fetch();

        assertThat(members)
                .hasSize(3)
                .extracting("username", "age")
                .containsExactly(
                        tuple("member5", 100),
                        tuple("member6", 100),
                        tuple(null, 100)
                );
    }

    @Test
    void paging1() {
        List<Member> members = factory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(2)
                .limit(2)
                .fetch();

        assertThat(members)
                .hasSize(2)
                .extracting("username", "age")
                .containsExactly(
                        tuple("member2", 20),
                        tuple("member1", 10)
                );
    }

    @Test
    void paging2() {
        QueryResults<Member> queryResults = factory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(2)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(2);
        assertThat(queryResults.getResults()).hasSize(2);
    }

    @Test
    void aggregation() {
        List<Tuple> tuples = factory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = tuples.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
    }

    /*
    * 팀의 이름과 각 팀의 평균 연령
    * */
    @Test
    void group() {
        List<Tuple> tuples = factory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        for (Tuple tuple : tuples) {
            System.out.println("tuple.get(team.name) = " + tuple.get(team.name));
            System.out.println("tuple.get(member.age.avg()) = " + tuple.get(member.age.avg()));
        }

        Tuple teamA = tuples.get(0);
        Tuple teamB = tuples.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }


    /*
    * 팀 A에 소속된 모든 회원
    * */
    @Test
    void join() {
        /*List<Member> members = factory
                .selectFrom(member)
                .where(member.team.name.eq("teamA"))
                .fetch();*/

        List<Member> members = factory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(members)
                .extracting("username", "team.name")
                .containsExactly(
                        tuple("member1", "teamA"),
                        tuple("member2", "teamA")
                );
    }

    /*
    * Theta Join
    * 회원의 이름이 팀 이름과 같은 회원 조회
    * */
    @Test
    void thetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> members = factory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(members)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /*
     * 회원과 팀을 조인하면서 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
     * */
    @Test
    void joinOnFiltering() {
        List<Tuple> tuples = factory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);
        }
    }

    /*
     * 연관관계 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     * */
    @Test
    void joinOnNoRelation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamㅊ"));

        List<Tuple> tuples = factory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void noFetchJoin() {
        em.flush();
        em.clear();

        Member findMember = factory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember).isNotNull();
        assertThat(emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam())).isFalse();
    }

    @Test
    void fetchJoin() {
        em.flush();
        em.clear();

        Member findMember = factory
                .selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember).isNotNull();
        assertThat(emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam())).isTrue();
    }

    /*
    * 나이가 가장 많은 회원 조회
    * */
    @Test
    void subQuery() {

        QMember memberSub = new QMember("memberSub");

        Member findMember = factory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                        .from(memberSub)
                ))
                .fetchOne();

        assertThat(findMember)
                .extracting("username", "age")
                .containsExactly("member4", 40);
    }

    /*
     * 나이가 평균 이상인 회원
     * */
    @Test
    void subQueryGoe() {
        QMember memberSub = new QMember("memberSub");

        List<Member> members = factory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                        .from(memberSub)
                ))
                .fetch();

        assertThat(members)
                .hasSize(2)
                .extracting("username")
                .containsExactly("member3", "member4");
    }

    @Test
    void subQueryIn() {
        QMember memberSub = new QMember("memberSub");

        List<Member> members = factory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                        .from(memberSub)
                        .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(members)
                .hasSize(3)
                .extracting("username")
                .containsExactly("member2", "member3", "member4");
    }

    @Test
    void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Tuple> tuples = factory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .fetch();

        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void basicCase() {
        List<String> ages = factory
                .select(member.age
                        .when(10).then("ten")
                        .when(20).then("twenty")
                        .otherwise("other"))
                .from(member)
                .fetch();

        for (String age : ages) {
            System.out.println("age = " + age);
        }
    }

    @Test
    void complexCase() {
        List<String> ages = factory
                .select(
                        new CaseBuilder()
                                .when(member.age.between(0, 20)).then("0-20")
                                .when(member.age.between(21, 30)).then("21-30")
                                .otherwise("other")
                )
                .from(member)
                .fetch();

        for (String age : ages) {
            System.out.println("age = " + age);
        }
    }

    @Test
    void constant() {
        List<Tuple> tuples = factory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void concat() {
        String s = factory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("s = " + s);
    }

}
