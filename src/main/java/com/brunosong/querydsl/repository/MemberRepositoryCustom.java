package com.brunosong.querydsl.repository;

import com.brunosong.querydsl.dto.MemberSearchCondition;
import com.brunosong.querydsl.dto.MemberTeamDto;

import java.util.List;

/* 이넘에 이름은 아무렇게나 적어도 상관없다. */
public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);

}
