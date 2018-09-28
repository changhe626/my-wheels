package com.onyx.spring.ioc0;


import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.lang.reflect.Field;
import java.util.List;


/**
 * 获取bean
 */
public class ClassPathXmlApplicationContext  implements BeanFactory {
    /**
     * 配置文件路径
     */
    private String xmlPath;

    public ClassPathXmlApplicationContext(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    /**
     * 获取bean
     * @param beanId  beanId
     * @return  封装完参数的类
     */
    @Override
    public Object getBean(String beanId) throws Exception {
        //1、读取xml配置文件
        // 获取xml解析器
        SAXReader saxReader = new SAXReader();
        // 获得document对象
        Document read = saxReader.read(this.getClass().getClassLoader().getResourceAsStream(xmlPath));
        // 获得根节点
        Element rootElement = read.getRootElement();
        System.out.println("根节点的名称: " + rootElement.getName());
        //获得元素对象
        List<Element> elements = rootElement.elements();
        Object obj = null;
        for (Element sonElement : elements) {
            //2、获取到每个bean配置，获得class地址
            //获得每个bean配置 获取class地址
            String sonBeanId = sonElement.attributeValue("id");
            //循环遍历,是bean的id下一步,不是的话继续循环
            if (!beanId.equals(sonBeanId)) {
                continue;
            }
            String beanClassPath = sonElement.attributeValue("class");
            //3、拿到class地址，进行反射技术实例化对象，使用反射api为私有属性赋值
            Class<?> clazz = Class.forName(beanClassPath);
            obj = clazz.newInstance();
            //拿到成员属性
            List<Element> elementList = sonElement.elements();
            for (Element element : elementList) {
                String name = element.attributeValue("name");
                String value = element.attributeValue("value");
                //使用反射技术为私有属性赋值
                Field declaredField = clazz.getDeclaredField(name);
                //强制往私有属性赋值,应该是用set方法进行赋值,这样偷了个懒
                declaredField.setAccessible(true);
                declaredField.set(obj, value);
            }
        }
        if(obj==null){
            throw new RuntimeException("没有id为:"+beanId+"的bean");
        }
        return obj;
    }


    @Override
    public <T> T getBean(Class<T> requiredType) throws Exception {
        //1、读取xml配置文件
        // 获取xml解析器
        SAXReader saxReader = new SAXReader();
        // 获得document对象
        Document read = saxReader.read(this.getClass().getClassLoader().getResourceAsStream(xmlPath));
        // 获得根节点
        Element rootElement = read.getRootElement();
        System.out.println("根节点的名称: " + rootElement.getName());
        //获得元素对象
        Object object=null;
        List<Element> elements = rootElement.elements();
        for (Element element : elements) {
            String aClass = element.attributeValue("class");
            Class<?> clazz = Class.forName(aClass);
            if(!requiredType.equals(clazz)){
                continue;
            }
            object = clazz.newInstance();
            //找到了就不再循环了...
            break;
        }
        return (T)object;
    }



}