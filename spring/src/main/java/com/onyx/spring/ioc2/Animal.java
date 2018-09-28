package com.onyx.spring.ioc2;

@Bean
public class Animal {

    @Autowired(beanName = "aaperson")
    private Person person;


}
