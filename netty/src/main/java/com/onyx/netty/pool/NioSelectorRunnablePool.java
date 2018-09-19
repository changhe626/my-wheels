package com.onyx.netty.pool;

import com.onyx.netty.NioServerBoss;
import com.onyx.netty.NioServerWorker;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * selector线程管理者
 *
 */
public class NioSelectorRunnablePool {

	/**
	 * boss线程数组
	 */
	private final AtomicInteger bossIndex = new AtomicInteger();
	private Boss[] bosses;

	/**
	 * worker线程数组
	 */
	private final AtomicInteger workerIndex = new AtomicInteger();
	private Worker[] workeres;

	
	public NioSelectorRunnablePool(Executor boss, Executor worker) {
		initBoss(boss, 1);
		//CPU的核心数*2
		initWorker(worker, Runtime.getRuntime().availableProcessors() * 2);
	}


	/**
	 * 初始化boss线程
	 */
	private void initBoss(Executor boss, int count) {
		this.bosses = new NioServerBoss[count];
		for (int i = 0; i < bosses.length; i++) {
			//每个数组中设置boss线程池
			bosses[i] = new NioServerBoss(boss, "boss thread " + (i+1), this);
		}
	}


	/**
	 * 初始化worker线程
	 */
	private void initWorker(Executor worker, int count) {
		this.workeres = new NioServerWorker[count];
		for (int i = 0; i < workeres.length; i++) {
			workeres[i] = new NioServerWorker(worker, "worker thread " + (i+1), this);
		}
	}

	/**
	 * 获取一个worker
     * 转圈循环获取,这是使用的负载均衡,求余的方法,平均分配
	 */
	public Worker nextWorker() {
		 return workeres[Math.abs(workerIndex.getAndIncrement() % workeres.length)];

	}

	/**
	 * 获取一个boss
	 */
	public Boss nextBoss() {
		 return bosses[Math.abs(bossIndex.getAndIncrement() % bosses.length)];
	}

}
