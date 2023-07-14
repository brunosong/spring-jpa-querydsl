package com.brunosong.querydsl.repository;

import com.brunosong.querydsl.dto.MemberSearchCondition;
import com.brunosong.querydsl.dto.MemberTeamDto;
import com.brunosong.querydsl.dto.QMemberTeamDto;
import com.brunosong.querydsl.entity.Member;
import com.brunosong.querydsl.entity.QMember;
import com.brunosong.querydsl.entity.QTeam;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.brunosong.querydsl.entity.QMember.*;
import static com.brunosong.querydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.*;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    /* 순수한 JPA 로 구성 */
    public void save(Member member) {
        em.persist(member);
    }

    public List<Member> findAll() {
        List<Member> members = em.createQuery(
                        "select m from Member m", Member.class)
                .getResultList();

        return members;
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);

        return Optional.ofNullable(member);
    }

    public List<Member> findByUsername(String username) {

        List<Member> members = em.createQuery(
                        "select m from Member m where m.username = :username", Member.class)
                .setParameter("username",username)
                .getResultList();

        return members;
    }


    /* querydsl 로 구성 */
    public List<Member> findAll_querydsl() {
        List<Member> members =  queryFactory
                .select(member)
                .from(member)
                .fetch();

        return members;
    }


    public Optional<Member> findById_querydsl(Long id) {

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(findMember);
    }


    public List<Member> findByUsername_querydsl(String username) {

        List<Member> members = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq(username))
                .fetch();

        return members;
    }

    //Builder 사용
    //회원명, 팀명, 나이(ageGoe, ageLoe)
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

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

        List<MemberTeamDto> members = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();

        return members;

    }


    public List<MemberTeamDto> search(MemberSearchCondition condition) {

        List<MemberTeamDto> members = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();

        return members;

    }


    //where 파라미터 방식은 이런식으로 재사용이 가능하다.
    public List<Member> findMember(MemberSearchCondition condition) {
        return queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }


    private BooleanExpression usernameEq(String username) {
        return isEmpty(username) ? null : member.username.eq(username);
    }
    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }
    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }



}
