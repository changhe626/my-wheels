package com.onyx.rpc.rpc0;

public class HelloServiceImpl implements HelloService {

    public String hello(String name) {  
        return "Hello " + name;  
    }  
  
} 