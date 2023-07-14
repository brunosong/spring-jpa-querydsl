package com.brunosong.querydsl.repository;

import com.brunosong.querydsl.dto.MemberSearchCondition;
import com.brunosong.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/* 이넘에 이름은 아무렇게나 적어도 상관없다. */
public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);

    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);

}
