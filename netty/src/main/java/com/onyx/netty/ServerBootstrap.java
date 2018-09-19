package com.onyx.netty;

import com.onyx.netty.pool.Boss;
import com.onyx.netty.pool.NioSelectorRunnablePool;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * 服务类
 *
 */
public class ServerBootstrap {

	private NioSelectorRunnablePool selectorRunnablePool;
	
	public ServerBootstrap(NioSelectorRunnablePool selectorRunnablePool) {
		this.selectorRunnablePool = selectorRunnablePool;
	}
	
	/**
	 * 绑定端口
	 * @param port 端口号
	 */
	public void bind(int port){
		InetSocketAddress localAddress = new InetSocketAddress(port);
		try {
			// 获得一个ServerSocket通道
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			// 设置通道为非阻塞
			serverChannel.configureBlocking(false);
			// 将该通道对应的ServerSocket绑定到port端口
			serverChannel.socket().bind(localAddress);
			//获取一个boss线程
			Boss nextBoss = selectorRunnablePool.nextBoss();
			//向boss注册一个ServerSocket通道
			nextBoss.registerAcceptChannelTask(serverChannel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
