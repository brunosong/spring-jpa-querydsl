알아둘점
=============

> spring.jpa.properties.hibernate.show_sql: true
>   >   <pre><code> 
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
>   >   </code></pre>
>   이런식으로 두번이 나가는것 처럼 보이게 되어서 show_sql 은 System.out 이기 때문에 주석을 처리해준다. (디버거꺼를 사용하게 한다.)

> 로그 쿼리 바인딩 (?) 처리 방법 
>   > 1. org.hibernate.type: trace 로 바꿔준다.
>   > 2. implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.8.1' 를 추가한다. (외부라이브러리 사용)
>   > 3. 결과 : insert into hello (id) values (1); 
>   > 4. 운영에서는 쓸때는 성능을 생각해야 해서 남길지는 선택을 해야 한다.


> create-drop
>   > 어플리케이션 로드 시점에 테이블 삭제를 하고 다시 테이블을 만든다. create-drop 은 종료시에 테이블도 다 지운다.







