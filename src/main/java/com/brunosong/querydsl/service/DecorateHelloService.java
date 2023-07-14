package com.brunosong.querydsl.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class DecorateHelloService implements HelloService {

    private final HelloService helloService;

    public DecorateHelloService(HelloService helloService) {
        this.helloService = helloService;
    }

    public String sayHello(String name){
        return "*" + helloService.sayHello(name) + "*";
    }

}
