package com.onyx.spring.ioc0;

/**
 * @author zk
 * @Description: bean工厂
 * @date 2018-09-12 14:05
 */
public interface BeanFactory {


    /**
     * 根据名字获取bean
     * @param name
     * @return
     * @throws Exception
     */
    Object getBean(String name) throws Exception;


    /**
     * 根据类型获取bean
     * @param requiredType
     * @param <T>
     * @return
     */
    <T> T getBean(Class<T> requiredType) throws Exception;


}
