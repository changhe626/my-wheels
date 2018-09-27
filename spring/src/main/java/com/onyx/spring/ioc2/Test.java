package com.onyx.spring.ioc2;

public class Test {

    public static void main(String[] args) {


        try {
            ProjectScanner.init("com.onyx.spring.ioc2");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }


}
