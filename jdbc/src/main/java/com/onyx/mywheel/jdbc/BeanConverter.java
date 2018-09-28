package com.onyx.mywheel.jdbc;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zk
 * @Description: Map 和Bean 的相互转换
 * @date 2018-09-27 10:54
 */
public class BeanConverter {

    /**
     * bean 变成map
     * @return
     */
    public static <T> Map<String, Object> bean2map(T bean, Class<? extends T> clazz) throws Exception {
        Map<String, Object> map = new HashMap<>();
        // 获取指定类（Person）的BeanInfo 对象
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
        // 获取所有的属性描述器
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            String key = pd.getName();
            Method getter = pd.getReadMethod();
            Object value = getter.invoke(bean);
            map.put(key, value);
        }
        return map;
    }


    public static <T> T map2bean(Map<String, Object> map, Class<T> clazz) throws Exception {
        // 创建JavaBean对象
        T obj = clazz.newInstance();
        // 获取指定类的BeanInfo对象
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
        // 获取所有的属性描述器
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            Object value = map.get(pd.getName());
            Method setter = pd.getWriteMethod();
            setter.invoke(obj, value);
        }
        return obj;
    }


}
