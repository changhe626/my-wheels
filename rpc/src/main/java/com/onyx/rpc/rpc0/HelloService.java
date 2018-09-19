package com.onyx.rpc.rpc0;

/**
 *
 * HelloService没有序列化，但是也不报错，为什么？
 * 为什么需要序列化？消费者和生产者所在的项目都有这个接口，只需要传输调用的方法名和传入的参数就行了。
 *
 * 1. 服务端 接受客户端来的socket流， 接受约定为
    1.1 方法名
    1.2 参数类型
    1.3 方法所需参数

   2. 客户端动态代理生成 代理service,调用该service的方法实则 交给invoke方法处理逻辑，在该逻辑中实现远程连接，起多个线程。
 */  
public interface HelloService {  
  
    String hello(String name);  
  
}  