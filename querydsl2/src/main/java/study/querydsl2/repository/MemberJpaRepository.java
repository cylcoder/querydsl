package study.querydsl2.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl2.dto.MemberSearchCondition;
import study.querydsl2.dto.MemberTeamDto;
import study.querydsl2.dto.QMemberTeamDto;
import study.querydsl2.entity.Member;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.*;
import static study.querydsl2.entity.QMember.member;
import static study.querydsl2.entity.QTeam.team;

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

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
        /*
        * BooleanBuilder를 사용하는 경우 아래와 같이 불필요한 코드가 발생한다.
        * */
        BooleanBuilder builder = new BooleanBuilder();

        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }

        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }

        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }

        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return factory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName"))
                )
                .from(member)
                .join(member.team, team)
                .where(builder)
                .fetch();
    }

    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        /*
        * Where절 파라미터를 사용하는 경우 직관적이고 깔끔한 쿼리문을 볼 수 있다.
        * */
        return factory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName"))
                )
                .from(member)
                .join(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression ageBetween(int ageGoe, int ageLoe) {
        return ageGoe(ageGoe).and(ageLoe(ageLoe));
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

}
