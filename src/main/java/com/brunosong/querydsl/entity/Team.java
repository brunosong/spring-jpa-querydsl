package com.brunosong.querydsl.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 기본 생성자인데 protected 를 만들어 준다.
@ToString(of = {"id","name"})
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name;

    @OneToMany(fetch = FetchType.LAZY , mappedBy = "team")  //연관관계의 주인을 지정해줘야 한다. 이넘은 연관관계의 주인이 아니다.
    private List<Member> members = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}
