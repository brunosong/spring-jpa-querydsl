package com.brunosong.querydsl.repository;

import com.brunosong.querydsl.dto.MemberSearchCondition;
import com.brunosong.querydsl.dto.MemberTeamDto;
import com.brunosong.querydsl.entity.Member;
import com.brunosong.querydsl.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository repository;

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
    void basic_test() {

        Member member = new Member("brunosong", 40);
        repository.save(member);

        Member findMember = repository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = repository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = repository.findByUsername("brunosong");

        assertThat(result2).containsExactly(member);

    }


    @Test
    void search_test() {

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        //List<MemberTeamDto> result = repository.searchByBuilder(condition);
        List<MemberTeamDto> result = repository.search(condition);

        assertThat(result).extracting("username").containsExactly("member4");

    }


    @Test
    void search_page_simple_test() {

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> memberPage = repository.searchPageSimple(condition, pageRequest);

        assertThat(memberPage.getSize()).isEqualTo(3);
        assertThat(memberPage.getTotalPages()).isEqualTo(2);
        assertThat(memberPage.getContent()).extracting("username")
                .containsExactly("member1","member2","member3");

        pageRequest = PageRequest.of(1, 3);

        Page<MemberTeamDto> memberPage2 = repository.searchPageSimple(condition, pageRequest);
        assertThat(memberPage2.getContent().size()).isEqualTo(1);
        assertThat(memberPage2.getContent()).extracting("username")
                .containsExactly("member4");

    }


    @Test
    void search_page_complex_test() {

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> memberPage = repository.searchPageSimple(condition, pageRequest);

        assertThat(memberPage.getSize()).isEqualTo(3);
        assertThat(memberPage.getTotalPages()).isEqualTo(2);
        assertThat(memberPage.getContent()).extracting("username")
                .containsExactly("member1","member2","member3");


    }



}