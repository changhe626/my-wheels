package com.onyx.spring.ioc3;

import com.onyx.spring.ioc2.Animal;
import com.onyx.spring.ioc2.Person;

public class Start {

    public static void main(String[] args)  {
        String path="com.onyx.spring.ioc2";
        ProjectScanner scanner = new ProjectScanner(path);
        scanner.init();
        scanner.injectProperty();

        AnnBeanFactory factory = new AnnBeanFactory(scanner);
        Person person = (Person) factory.getBean("person");
        System.out.println(person);
        Animal bean = factory.getBean(Animal.class);
        System.out.println(bean);
    }


}
