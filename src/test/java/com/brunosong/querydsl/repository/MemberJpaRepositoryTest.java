package com.brunosong.querydsl.repository;

import com.brunosong.querydsl.dto.MemberSearchCondition;
import com.brunosong.querydsl.dto.MemberTeamDto;
import com.brunosong.querydsl.entity.Member;
import com.brunosong.querydsl.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository repository;

    @Autowired
    EntityManager em;

    @BeforeEach
    public void setup() {

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
    void jpa_repository_test() {

        Member member = new Member("brunosong", 40);
        repository.save(member);

        Member findMember = repository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> members = repository.findAll();
        assertThat(members.size()).isEqualTo(5);

        List<Member> brunosongNames = repository.findByUsername("brunosong");

        assertThat(brunosongNames).containsExactly(member);

    }


    @Test
    void querydsl_repository_test() {

        Member member = new Member("brunosong", 40);
        repository.save(member);

        Member findMember = repository.findById_querydsl(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> members = repository.findAll_querydsl();
        assertThat(members.size()).isEqualTo(5);

        List<Member> brunosongNames = repository.findByUsername_querydsl("brunosong");

        assertThat(brunosongNames).containsExactly(member);

    }


    @Test
    public void searchTest() {

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        //List<MemberTeamDto> result = repository.searchByBuilder(condition);
        List<MemberTeamDto> result = repository.search(condition);

        assertThat(result).extracting("username").containsExactly("member4");

    }


}