package com.brunosong.querydsl;

import com.brunosong.querydsl.dto.MemberDto;
import com.brunosong.querydsl.dto.QMemberDto;
import com.brunosong.querydsl.dto.UserDto;
import com.brunosong.querydsl.entity.Member;
import com.brunosong.querydsl.entity.QMember;
import com.brunosong.querydsl.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static com.brunosong.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslMiddleTest {


    @Autowired
    EntityManager em;

    JPAQueryFactory jPAQueryFactory;

    @BeforeEach
    public void setup() {
        jPAQueryFactory = new JPAQueryFactory(em);

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
    public void simpleProjection() {

        List<String> result = jPAQueryFactory
                .select(member.username)    //여기 타입에 맞는 String 이 나온다. 이게 여러개면 Tuple로 나온다.
                .from(member)
                .fetch();


        for (String s : result) {
            System.out.println(" s :  " + s);
        }

    }


    @Test
    public void tupleProjection() {

        List<Tuple> fetch = jPAQueryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : fetch) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);

            System.out.println("username : "  +username);
            System.out.println("age : "  +age);
        }

    }


    @Test
    public void findDtoByJPQL() {
        /* 뉴오퍼레이션 문법이라고 한다. */
        List<MemberDto> resultList = em.createQuery(
                        "select new com.brunosong.querydsl.dto.MemberDto(m.username, m.age) from Member m ", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : resultList) {
            System.out.println(memberDto);
        }
    }


    @Test
    public void findDtoByQuerydsl() {

        List<MemberDto> resultList = jPAQueryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : resultList) {
            System.out.println(memberDto);
        }

    }


    @Test
    public void findDtoByField() {
        /* 겟터 셋터가 없이 필드에 바로 값을 박아버린다. private 인데 어떻게 하냐면 리플랙션으로 써서 하는것 같다. */
        List<MemberDto> resultList = jPAQueryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : resultList) {
            System.out.println(memberDto);
        }

    }


    @Test
    public void findDtoByConstructor() {
        /* 생성자랑 딱 맞춰줘야 한다. */
        List<MemberDto> resultList = jPAQueryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : resultList) {
            System.out.println(memberDto);
        }

    }


    @Test
    public void findUserDto() {
        QMember memberSub = new QMember("memberSub");

        List<UserDto> resultList = jPAQueryFactory
                .select(Projections.fields( UserDto.class,
                        member.username.as("name"),
                        /* 서브쿼리 같은 경우에는 방법이 없어서 이런식으로 알리아스를 줘야 한다. */
                        ExpressionUtils.as( JPAExpressions
                                    .select(memberSub.age.max())
                                    .from(memberSub), "age")
                ))
                .from(member)
                .fetch();

        for (UserDto memberDto : resultList) {
            System.out.println(memberDto);
        }

    }



    @Test
    public void findDtoByQueryProjection() {
        // 컴파일 시점에 오류를 잡아줘서 좋다. (컴파일 오류)
        List<MemberDto> resultList = jPAQueryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : resultList) {
            System.out.println(memberDto);
        }

    }






}
