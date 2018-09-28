package com.onyx.spring.ioc2;

public class Test {

    public static void main(String[] args) {


        try {
            ProjectScanner scanner = new ProjectScanner("com.onyx.spring.ioc2");
            scanner.init();
            scanner.injectProperty();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }


}
