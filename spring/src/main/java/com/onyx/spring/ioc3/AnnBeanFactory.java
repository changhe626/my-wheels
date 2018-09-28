package com.onyx.spring.ioc3;

import java.util.Map;

/**
 * 通过注解获取到beanFactory中的bean
 */
public class AnnBeanFactory implements BeanFactory {


    private ProjectScanner projectScanner;

    public AnnBeanFactory(ProjectScanner projectScanner) {
        this.projectScanner = projectScanner;
    }


    @Override
    public synchronized Object getBean(String name) {
        Map<String, Object> beans = projectScanner.getBeans();
        return beans.get(name);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        Map<String, Object> beans = projectScanner.getBeans();
        for (Object value : beans.values()) {
            if(value.getClass().equals(requiredType)){
                return (T)value;
            }
        }
        return null;
    }
}
