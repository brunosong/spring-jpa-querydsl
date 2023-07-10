package com.brunosong.querydsl;

import com.brunosong.querydsl.entity.Member;
import com.brunosong.querydsl.entity.QMember;
import com.brunosong.querydsl.entity.QTeam;
import com.brunosong.querydsl.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.brunosong.querydsl.entity.QMember.*;
import static com.brunosong.querydsl.entity.QTeam.*;
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



    @Test
    void resultFetch() {

        List<Member> fetch = jpaQueryFactory
                                .selectFrom(member)
                                .fetch();

        Member fetchOne = jpaQueryFactory
                .selectFrom(member)
                .fetchOne();

        Member fetchFirst = jpaQueryFactory
                .selectFrom(member)
                .fetchFirst();

        QueryResults<Member> result = jpaQueryFactory
                .selectFrom(member)
                .fetchResults();        //쿼리가 두번 실행된다. 왜냐면 토탈 카운트를 가져와야 한다.

        long total = result.getTotal();
        List<Member> contents = result.getResults();

        long totalCnt = jpaQueryFactory
                .selectFrom(member)
                .fetchCount();



    }


    /*
    *
    * 회원 정렬 순서
    * 1. 회원 나이 내림차순 ( desc )
    * 2. 회원 이름 올림차순 ( asc )
    * 단 2에서 회원 이름이 없으면 마지막에 출력 (nulls last)
    * */

    @Test
    public void sort() {
        em.persist(new Member(null , 100));
        em.persist(new Member("member5" , 100));
        em.persist(new Member("member6" , 100));

        List<Member> fetch = jpaQueryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                //.orderBy(member.age.desc(), member.username.asc().nullsFirst())  member1.username asc nulls last ,  member1.username asc nulls first 두개가 존재한다.
                .fetch();

        for (Member fetch1 : fetch) {
            System.out.println("memberName : " + fetch1.getUsername());
        }

        assertThat(fetch.get(0).getUsername()).isEqualTo("member5");
        assertThat(fetch.get(1).getUsername()).isEqualTo("member6");
        assertThat(fetch.get(2).getUsername()).isNull();

    }



    @Test
    public void paging1() {
        List<Member> fetch = jpaQueryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(0)  //몇개를 스킵하고 시작할꺼냐 묻는거임
                .limit(2)
                .fetch();

        assertThat(fetch.size()).isEqualTo(2);

    }


    @Test
    public void paging2() {
        QueryResults<Member> queryResults = jpaQueryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(0)  //몇개를 스킵하고 시작할꺼냐 묻는거임
                .limit(2)
                .fetchResults();



        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(0);

        assertThat(queryResults.getResults().size()).isEqualTo(2);

        //실무에서는 쓸수도 있고 못쓸수도 있다. 단순한 쿼리에서는 사용가능하나 복잡해지면 사용이 불가능 하다.
    }



    @Test
    public void aggregation(){
        List<Tuple> result = jpaQueryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                ).from(member)
                .fetch();

        Tuple tuple = result.get(0);

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);


    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    void group() {

        List<Tuple> fetch = jpaQueryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

    }
    










}
