package com.brunosong.querydsl;

import com.brunosong.querydsl.dto.MemberDto;
import com.brunosong.querydsl.dto.QMemberDto;
import com.brunosong.querydsl.dto.UserDto;
import com.brunosong.querydsl.entity.Member;
import com.brunosong.querydsl.entity.QMember;
import com.brunosong.querydsl.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static com.brunosong.querydsl.entity.QMember.*;
import static org.assertj.core.api.Assertions.*;

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

    @Test
    public void 동적쿼리_BooleanBuilder() throws Exception {

        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember1(usernameParam,ageParam);
        assertThat(result.size()).isEqualTo(1);

    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();

        if( usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }

        if( ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return jPAQueryFactory
                .select(member)
                .from(member)
                .where( builder )
                .fetch();

    }


    @Test
    public void 동적쿼리_WhereParam() throws Exception {

        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam,ageParam);
        assertThat(result.size()).isEqualTo(1);

    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return jPAQueryFactory
                .select(member)
                .from(member)
                .where( usernameEq(usernameCond) , ageEq(ageCond) )
                .fetch();
    }

    private Predicate ageEq(Integer ageCond) {
        return ageCond == null ? null : member.age.eq(ageCond);
    }

    private BooleanExpression usernameEq(String usernameCond) {
        //null 이 리턴이 되버리면 무시가 된다. 그래서 동적쿼리가 만들어 지는것이다.
        return usernameCond == null ? null : member.username.eq(usernameCond);
    }


    @Test
    public void bulkUpdate() {

        long count = jPAQueryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        //영속성 컨텍스트 상태와 디비에 상태가 달라져 있다.
        //영속성 컨텍스트에 값은 아직 member1 이 되어버린다.
        //디비에서 셀렉트를 해도 영속성 컨텍스트에 값이 있으면 디비에서 가져온 값을 버린다.
        /* 이렇게 가져오면 영속성 컨텍스트에 있는것으로 가지고 오게 되서 변경된 값을 가지고 오지 못한다. */
        List<Member> memberResult = jPAQueryFactory
                .selectFrom(member)
                .fetch();

        em.flush();
        em.clear();




        List<UserDto> resultList = jPAQueryFactory
                                .select(Projections.fields(UserDto.class,
                                        member.username.as("name"),
                                        member.age))
                                .from(member)
                                .fetch();

        for (UserDto user : resultList) {
            System.out.println(user);
        }

    }


    @Test
    void bulkAdd(){

        long count = jPAQueryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();

    }


    @Test
    void bulkDelete(){

        long execute = jPAQueryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();

    }


    @Test
    public void sqlFunction() {

        // 함수는 H2 Sql Dialect 에 정해진 함수만 호출할수 있다.
        // H2Dialect.class 에 정의 되어 있다.
        // String Functions ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // registerFunction( "ascii", new StandardSQLFunction( "ascii", StandardBasicTypes.INTEGER ) );
        // registerFunction( "char", new StandardSQLFunction( "char", StandardBasicTypes.CHARACTER ) );
        // registerFunction( "concat", new VarArgsSQLFunction( StandardBasicTypes.STRING, "(", "||", ")" ) );
        // registerFunction( "difference", new StandardSQLFunction( "difference", StandardBasicTypes.INTEGER ) );
        // registerFunction( "hextoraw", new StandardSQLFunction( "hextoraw", StandardBasicTypes.STRING ) );
        // registerFunction( "insert", new StandardSQLFunction( "lower", StandardBasicTypes.STRING ) );
        // registerFunction( "left", new StandardSQLFunction( "left", StandardBasicTypes.STRING ) );
        // registerFunction( "lcase", new StandardSQLFunction( "lcase", StandardBasicTypes.STRING ) );
        // registerFunction( "ltrim", new StandardSQLFunction( "ltrim", StandardBasicTypes.STRING ) );
        // registerFunction( "octet_length", new StandardSQLFunction( "octet_length", StandardBasicTypes.INTEGER ) );
        // registerFunction( "position", new StandardSQLFunction( "position", StandardBasicTypes.INTEGER ) );
        // registerFunction( "rawtohex", new StandardSQLFunction( "rawtohex", StandardBasicTypes.STRING ) );
        // registerFunction( "repeat", new StandardSQLFunction( "repeat", StandardBasicTypes.STRING ) );
        // registerFunction( "replace", new StandardSQLFunction( "replace", StandardBasicTypes.STRING ) );

        // 추가로 함수를 쓰고 싶다면 상속을 받아서 처리 하는 방법이 있다. 설정을 해서 쓰는것이다.
        List<String> result = jPAQueryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})"
                        , member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }

    }

    @Test
    public void sqlFunction2() {

        List<String> fetch = jPAQueryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq( Expressions.stringTemplate(
                        "function('lower' , {0} )", member.username)
                )).fetch();

        /* 공통적으로 안시 표준에 있는 함수는 기본적으로 다 제공이 된다. 많은 것들을 내장하고 있다. */
        List<String> fetch2 = jPAQueryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(member.username.lower()))
                .fetch();

    }



}
