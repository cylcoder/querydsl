package study.querydsl2.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl2.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member1", 20);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

//        Member findMember = memberJpaRepository.jpaFindById(member1.getId()).orElseThrow();
        Member findMember = memberJpaRepository.querydslFindById(member1.getId()).orElseThrow();
        assertThat(findMember).isEqualTo(member1);

//        List<Member> members1 = memberJpaRepository.jpaFindAll();
        List<Member> members1 = memberJpaRepository.querydslFindAll();

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
        List<Member> members2 = memberJpaRepository.querydslFindByUsername("member1");
        assertThat(members2).hasSize(2)
                .containsExactly(member1, member2);
    }

}