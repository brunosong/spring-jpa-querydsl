package com.brunosong.querydsl.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id","username","age"})   // team 이 들어가면 안된다. 계속 호출을 하게 되서 무한루프를 타게 된다. 연관관계얘들은 손대지 않는게 좋다.
@NamedEntityGraph(name = "Member.all", attributeNodes = @NamedAttributeNode("team"))
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    public Member(String username) {
        this(username,0,null);
    }

    public Member(String username, int age) {
        this(username,age,null);
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if(team != null) {
            changeTeam(team);
        }
        this.team = team;
    }

    @ManyToOne(fetch = FetchType.LAZY)   // Lazy를 해줘야 한다.  외래키 이름이다.
    @JoinColumn(name = "team_id")
    private Team team;

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
