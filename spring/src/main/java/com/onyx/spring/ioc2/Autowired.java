package com.onyx.spring.ioc2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Autowired {

    /**
     * 注入的bean的名字
     *
     * @return
     */
    public String beanName() default "";

}
