package org.curefun.jdbc.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenlei:
 * @version 创建时间：2018年8月20日 下午2:32:33 类说明
 */
public class BeanConverter {
	// JavaBean转换为Map
	public static <T> Map<String, Object> bean2map(T bean, Class<? extends T> classz) throws Exception {
		Map<String, Object> map = new HashMap<>();
		// 获取指定类（Person）的BeanInfo 对象
		BeanInfo beanInfo = Introspector.getBeanInfo(classz, Object.class);
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

	// Map转换为JavaBean
	public static <T> T map2bean(Map<String, Object> map, Class<T> clz) throws Exception {
		// 创建JavaBean对象
		T obj = clz.newInstance();
		// 获取指定类的BeanInfo对象
		BeanInfo beanInfo = Introspector.getBeanInfo(clz, Object.class);
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
