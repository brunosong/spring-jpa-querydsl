package com.brunosong.querydsl;

import com.brunosong.querydsl.entity.Hello;
import com.brunosong.querydsl.entity.QHello;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
@Commit         //디비에 데이터가 어떻게 들어가졌는지 볼때 사용한다. (실제로 눈으로 보고 싶을때)
class QuerydslApplicationTests {

    @Autowired
    EntityManager em;

    @Test
    void contextLoads() {


        Hello hello = new Hello();
        em.persist(hello);


        JPAQueryFactory query = new JPAQueryFactory(em);
        //QHello qHello = new QHello("h");
        QHello qHello = QHello.hello;

        Hello result = query.selectFrom(qHello)
                .fetchOne();





    }

}
