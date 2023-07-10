package com.brunosong.querydsl;

import com.brunosong.querydsl.entity.Member;
import com.brunosong.querydsl.entity.QMember;
import com.brunosong.querydsl.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static com.brunosong.querydsl.entity.QMember.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {


    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    void setup(){
        jpaQueryFactory = new JPAQueryFactory(em);  //멀티쓰레드에 아무 문제가 없게 설계가 되어있다. 동시성 문제가 없다. 필드로 빼서 쓰는것을 권장한다.
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1",10, teamA);
        Member member2 = new Member("member2",20, teamA);

        Member member3 = new Member("member3",30, teamB);
        Member member4 = new Member("member4",40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

    }



    @Test
    void startJPQL() {

        //member1 을 찾아라
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();


        assertThat(findMember.getUsername()).isEqualTo("member1");

    }



    @Test
    void startQuerydsl() {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QMember m = new QMember("m1");    // 같은 테이블을 조인해야 할경우가 생길수 있으니 m1 , m2로 해서 설정해 주면 된다.

        Member findMember = jpaQueryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }


    @Test
    void startQuerydsl2() {
        //Querydsl은 결국에는 JPQL로 변환되어서 나간다고 생각하면 된다.
        Member findMember = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }



    @Test
    void search() {
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }


    @Test
    void searchAndParam() {
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(
                    member.username.eq("member1"),
                    member.age.eq(10)
                )   // AND 는 , 도 가능하다. AND만 있는경우 이경우를 더 선호한다. null 을 무시해준다. 동적쿼리때 설명해준다.
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }


}
