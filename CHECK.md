알아둘점
=============

> spring.jpa.properties.hibernate.show_sql: true
>   >   ```
>   >    2023-07-10 09:32:58.694 DEBUG 27944 --- [    Test worker] org.hibernate.SQL     
>   >    insert
>   >    into
>   >        hello
>   >        (id)
>   >    values
>   >        (?)
>   >    Hibernate:
>   >    insert
>   >    into
>   >        hello
>   >        (id)
>   >    values
>   >   ```
>   이런식으로 두번이 나가는것 처럼 보이게 되어서 show_sql 은 System.out 이기 때문에 주석을 처리해준다. (디버거꺼를 사용하게 한다.)

> spring.jpa.properties.hibernate.use_sql_comments: true
>   > /* select member1 from Member member1 where member1.username = ?1 */  #  
>   > 이것처럼 쿼리가 나갈때 커멘트를 남겨준다.

> 로그 쿼리 바인딩 (?) 처리 방법 
>   > 1. org.hibernate.type: trace 로 바꿔준다.
>   > 2. implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.8.1' 를 추가한다. (외부라이브러리 사용)
>   > 3. 결과 : insert into hello (id) values (1); 
>   > 4. 운영에서는 쓸때는 성능을 생각해야 해서 남길지는 선택을 해야 한다.


> create-drop
>   > 어플리케이션 로드 시점에 테이블 삭제를 하고 다시 테이블을 만든다. create-drop 은 종료시에 테이블도 다 지운다.


> JQPL이 제공하는 모든 검색 조건 제공
>   > ``` 
>   > member.username.eq("member1") // username = 'member1'
>   > member.username.ne("member1") // username != 'member1'
>   > member.username.eq("member1").not() // username != 'member1'
>   > 
>   > member.username.isNotNull() //이름이 is not null
>   > 
>   > member.age.in(10,20) // age in (10,20)
>   > member.age.notIn(10,20) // age not in (10,20)
>   > member.age.between(10,30) // between 10, 30
>   > 
>   > member.age.goe(30) // age >= 30
>   > member.age.gt(30)  // age > 30
>   > member.age.loe(30) // age <= 30
>   > member.age.lt(30)  // age < 30
>   > 
>   > member.username.like("member%") 
>   > member.usernmae.contains("member") // like '%member%' 검색
>   > member.usernmae.startWith("member") // like 'member%' 검색








