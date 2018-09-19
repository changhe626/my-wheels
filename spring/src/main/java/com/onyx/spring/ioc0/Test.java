package com.onyx.spring.ioc0;

/**
 * @author zk
 * @Description:
 * @date 2018-09-12 14:12
 */
public class Test {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ioc0/user.xml");
        UserEntity user = (UserEntity) context.getBean("user1");
        System.out.println(user);

        UserEntity bean = context.getBean(UserEntity.class);
        System.out.println(bean);

    }
}
