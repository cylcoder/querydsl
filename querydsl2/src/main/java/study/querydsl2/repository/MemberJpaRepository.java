package study.querydsl2.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl2.entity.Member;

import java.util.List;
import java.util.Optional;

import static study.querydsl2.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager manager;
    private final JPAQueryFactory factory;

    public void save(Member member) {
        manager.persist(member);
    }

    public Optional<Member> jpaFindById(Long id) {
        Member member = manager.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public Optional<Member> querydslFindById(Long id) {
        return Optional.ofNullable(
                factory
                        .selectFrom(member)
                        .where(member.id.eq(id))
                        .fetchOne()
        );
    }

    public List<Member> jpaFindAll() {
        return manager.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> querydslFindAll() {
        return factory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> jpaFindByUsername(String username) {
        return manager.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> querydslFindByUsername(String username) {
        return factory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

}
