package com.brunosong.querydsl.repository;

import com.brunosong.querydsl.entity.Member;
import com.brunosong.querydsl.entity.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.brunosong.querydsl.entity.QMember.*;

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





}
