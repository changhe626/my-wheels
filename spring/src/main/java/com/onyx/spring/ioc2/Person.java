package com.onyx.spring.ioc2;

@Bean(name = "aaperson")
public class Person {

    @Autowired
    private Animal animal;


}
