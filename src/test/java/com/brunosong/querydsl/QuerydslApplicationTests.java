package com.brunosong.querydsl;

import com.brunosong.querydsl.entity.Hello;
import com.brunosong.querydsl.entity.QHello;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
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
