package study.querydsl2;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl2.entity.Member;
import study.querydsl2.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl2.entity.QMember.member;

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

}
