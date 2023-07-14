package com.brunosong.querydsl.service;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Test
@interface UnitTest {}


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@UnitTest
@interface FastUnitTest {}


public class HelloServiceTest {

    @FastUnitTest
    void service_test(){

        SimpleHelloService helloService = new SimpleHelloService();

        String brunosong = helloService.sayHello("brunosong");

        Assertions.assertThat(brunosong).isEqualTo("hello brunosong");

    }

    @FastUnitTest
    void decorate_test(){

        DecorateHelloService helloService = new DecorateHelloService( name -> name );

        String brunosong = helloService.sayHello("brunosong");

        Assertions.assertThat(brunosong).isEqualTo("*brunosong*");

    }

}