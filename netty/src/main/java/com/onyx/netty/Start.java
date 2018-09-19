package com.onyx.netty;

import com.onyx.netty.pool.NioSelectorRunnablePool;

import java.util.concurrent.Executors;

/**
 * 启动主函数
 */
public class Start {

	public static void main(String[] args) {
		//初始化线程
		NioSelectorRunnablePool nioSelectorRunnablePool = new NioSelectorRunnablePool(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		//获取服务类
		ServerBootstrap bootstrap = new ServerBootstrap(nioSelectorRunnablePool);
		//绑定端口
		bootstrap.bind(10010);
		System.out.println("start");
	}
}
