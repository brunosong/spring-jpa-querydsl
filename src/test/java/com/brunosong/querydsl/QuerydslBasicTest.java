package com.brunosong.querydsl;

import com.brunosong.querydsl.entity.Member;
import com.brunosong.querydsl.entity.QMember;
import com.brunosong.querydsl.entity.QTeam;
import com.brunosong.querydsl.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.brunosong.querydsl.entity.QMember.*;
import static com.brunosong.querydsl.entity.QTeam.*;
import static com.querydsl.jpa.JPAExpressions.*;
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

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() {
        List<Member> teamA = jpaQueryFactory
                .selectFrom(member)
                //.join(member.team, team)
                .leftJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        for (Member member1 : teamA) {
            System.out.println(member1);
        }

        assertThat(teamA.size()).isEqualTo(2);

        assertThat(teamA)
                .extracting("username")
                .containsExactly("member1","member2");

    }


    /**
     *
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     * CROSS JOIN 같음
     */
    @Test
    public void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> fetch = jpaQueryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        // 외부 조인 불가능 : 최신버전에 들어가면서 이제 가능해짐 (2.1 부터 사용가능해짐)
        
        assertThat(fetch)
                .extracting("username")
                .containsExactly("teamA","teamB");

    }


    /**
     * 회원과 팀을 조인하면서 팀 이름이 teamA 인 팀만 조인 , 회원은 모두 조회
     * JPQL : select m , t from Member m left join m.team t on t.name = 'teamA'
     *
     */
    @Test
    public void join_on_filtering() {

        List<Tuple> result = jpaQueryFactory
                .select(member, team)
                .from(member)
                //.leftJoin(member.team, team)
                .join(member.team, team)  //기본이 inner join 이다.
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println(tuple);
        }
    }


    /**
     * 연관관계가 없는 엔티티 외부 조인
     * 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     *
     */
    @Test
    public void join_on_no_relation() {

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))         //막조인이다.
                .fetch();

        for (Tuple tuple : result) {
            System.out.println(tuple);
        }

    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() {

        em.flush();
        em.clear();

        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());   // 로드가 되었냐 안되었냐를 확인하는 메서드
        assertThat(loaded).as("패치조인미적용").isFalse();

    }



    @Test
    public void fetchJoinUse() {

        em.flush();
        em.clear();

        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());   // 로드가 되었냐 안되었냐를 확인하는 메서드
        assertThat(loaded).as("패치조인 적용").isTrue();

        System.out.println(findMember.getTeam().getName());  // 셀렉트가 한번 더 안나감

    }



    @Test
    public void fetchJoinUse_궁금해서() {

        em.flush();
        em.clear();

        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());   // 로드가 되었냐 안되었냐를 확인하는 메서드
        assertThat(loaded).as("패치조인 적용").isFalse();

        System.out.println(findMember.getTeam().getName());  // 샐럭트가 한번더 나감

    }


    /**
     * 나이가 가장 많은 회원 조회
     *
     */
    @Test
    public void subQuery() {

        //select * from member m where m.age = ( select max(subM.age) from member subM )
        // 서브쿼리는 쿼리 안에 쿼리를 넣겠다는 거
        QMember memberSub = new QMember("memberSub");

        List<Member> members = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();


        assertThat(members)
                .extracting("age")
                .containsExactly(40);


    }



    /**
     * 나이가 평균 이상인 사람
     *  select * from member m where m.age > (select avg(age) from member)
     */
    @Test
    public void subQuery_goe() {
        QMember subMember = new QMember("subMember");

        List<Member> members = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.age.goe(
                        select(subMember.age.avg())
                                .from(subMember)
                )).fetch();


        assertThat(members)
                .extracting("age")
                .containsExactly(30,40);


    }


    /**
     * 서브쿼리 여러 건 처리, in 사용
     */
    @Test
    public void subQueryIn() {

        QMember subMember = new QMember("subMember");

        List<Member> members = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.age.in(
                        select(subMember.age)
                                .from(subMember)
                                .where(subMember.age.gt(10))
                ))
                .fetch();

        assertThat(members)
                .extracting("age")
                .containsExactly(20, 30, 40);

    }

    /*
    *
    * JPA는 FROM 절에서는 지원하지 않는다. (인라인뷰)
    * 1. 서브쿼리를 join으로 변경한다. ( 가능한 상황도 불가능한 상황도 있다. )
    * 2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다. (상황에 따라 다르다 일반적으로는 2번 호출해도 크게 문제 되지 않는다.)
    * 3. nativeSQL 을 사용한다.
    *
    * from 절에 서브쿼리를 사용하는 이유
    *
    * 개발자가 어떻게든 화면에 맞춰서 한방 쿼리로 짤려고 할라고 하니깐 프롬절에 서브쿼리가 많이 사용될수 있다.
    * 하지만 이것을 피하기 위해서는 여러가지를 생각해볼수 있다.
    * 1. 쿼리를 분리해서 여러번 호출한다.
    *
    *
    * 현대적인 어플리케이션은 되도록이면 어플리케이션(비지니스) 로직에서 풀고 뷰는 프리젠테이션 로직에서 풀라고 하는 상황이다.
    * - SQL은 데이터를 가져오는데 집중을 하고 뭔가 이쁘게 만들고 그러는것은 화면에서 해야 한다.
    * - 디비는 데이터만 필터링 하고 그룹핑하고 가져오고 그정도 용도로 써야 한다.
    * - 디비는 데이터를 퍼올리는 용도로만 사용해야 한다.
    * - 각자 역활에 충실해질 필요가 있다. 내가 생각을 해도 대체적으로 디비에 부하가 생기지
    * - 와스단에서 부하가 생기는 경우는 거의 없다고 본다.
    * - 그리고 와스는 오토스케일링으로 빠르게 늘릴수 있다. 하지만 디비는 그렇지 않다. 한번 정해지면 정말 늘리기도 바꾸기도 힘들다.
    *
    *
    * 
    * 한방쿼리 미신 : 정말 잘되냐 ... 나도 고민이 많았다. 실시간 트래픽이 중요한 상황에서는 아까운데 요즘은 캐시로 발라서 쓰는데 
    * 어드민은 복잡한데 그런곳에서는 쿼리를 여러번 나눠서 만드는게 좋은 선택이다 ( 내 선택이 틀리지 않았다 ) SQL AntiPatterns 이란 책에서 나왔음 (https://www.yes24.com/Product/Goods/5269099)
    * 
    * 정말 복잡한 수백줄에 쿼리는 여러개 나눠서 작성하는게 좋다
    *
    * 참고
    *
    * 프리젠테이션 로직이란 말 그래도 보여주기 위한 로직을 말한다. 즉 화면상의 디자인 구성을 위한 로직을 일컫는 말로써, 게시판에서의 표시하기 위한 for(or while)문 등의 사용이 여기에 해당한다.
      반면에 비즈니스 로직이라는 것은 어떠한 특정한 값을 얻기 위해 데이터의 처리를 수행하는 응용프로그램의 일부를 말한다. 즉 원하는 값을 얻기 위해서 백엔드에서 일어나는 각종 처리를 일컫는 말이다.
      JSP와 자바 빈즈를 사용하는 구조에서 일반적으로 JSP는 프리젠테이션 로직을 담당하고 자바 빈즈는 비즈니스 로직을 담당한다.
    *
    * */


    @Test
    public void selectSubQuery() {

        QMember subMember = new QMember("subMember");

        List<Tuple> fetch = jpaQueryFactory
                .select(member.username,
                        select(subMember.age.avg())
                                .from(subMember))
                .from(member)
                .fetch();


        for (Tuple tuple : fetch) {
            System.out.println(tuple);
        }


    }



    @Test
    public void basicCase() {


        List<String> fetch = jpaQueryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("이십살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println(s);
        }

    }



    @Test
    public void complexCase() {


        List<String> fetch = jpaQueryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0,20)).then("0~20살")
                        .when(member.age.between(21,30)).then("21~31살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println(s);
        }

    }








}
