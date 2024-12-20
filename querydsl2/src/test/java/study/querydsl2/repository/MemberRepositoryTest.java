package study.querydsl2.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl2.dto.MemberSearchCondition;
import study.querydsl2.dto.MemberTeamDto;
import study.querydsl2.entity.Member;
import study.querydsl2.entity.Team;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager manager;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void basicTest() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member1", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

//        Member findMember = memberJpaRepository.jpaFindById(member1.getId()).orElseThrow();
        Member findMember = memberRepository.findById(member1.getId()).orElseThrow();
        assertThat(findMember).isEqualTo(member1);

//        List<Member> members1 = memberJpaRepository.jpaFindAll();
        List<Member> members1 = memberRepository.findAll();

        // 검증 방법 1
        assertThat(members1)
                .extracting("username", "age")
                .containsExactly(
                        tuple("member1", 10),
                        tuple("member1", 20)
                );

        // 검증 방법 2
        assertThat(members1).containsExactly(member1, member2);

//        List<Member> members2 = memberJpaRepository.jpaFindByUsername("member1");
        List<Member> members2 = memberRepository.findByUsername("member1");
        assertThat(members2).hasSize(2)
                .containsExactly(member1, member2);
    }

    @Test
    void searchTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        manager.persist(teamA);
        manager.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        manager.persist(member1);
        manager.persist(member2);
        manager.persist(member3);
        manager.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDto> result = memberRepository.search(condition);
        assertThat(result)
                .hasSize(1)
                .extracting("username", "age")
                .containsExactly(tuple("member4", 40));
    }

    @Test
    void searchPageSimple() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        manager.persist(teamA);
        manager.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        manager.persist(member1);
        manager.persist(member2);
        manager.persist(member3);
        manager.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<MemberTeamDto> page = memberRepository.searchPageSimple(condition, pageRequest);

        assertThat(page)
                .hasSize(3)
                .extracting("username")
                .containsExactly("member1", "member2", "member3");
    }

}