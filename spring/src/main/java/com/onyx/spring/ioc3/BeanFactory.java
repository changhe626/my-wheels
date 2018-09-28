package com.onyx.spring.ioc3;

/**
 * bean工厂
 */
public interface BeanFactory {

    /**
     * 根据名字获取bean
     * @param name
     */
    Object getBean(String name);


    /**
     * 根据类型获取bean
     * @param requiredType
     */
    <T> T getBean(Class<T> requiredType);

}
