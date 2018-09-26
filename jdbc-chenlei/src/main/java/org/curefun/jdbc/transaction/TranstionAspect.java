package org.curefun.jdbc.transaction;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.curefun.jdbc.JdbcUtil;

import java.sql.Connection;

@Aspect
public class TranstionAspect {

	private JdbcUtil jdbcUtil;
	private Logger loger = Logger.getLogger(getClass());

	@Pointcut("@annotation(org.curefun.jdbc.transaction.Transaction)")
	public void transAction() {
	}

	// @Before("transAction()")
	// public void doBefore(JoinPoint joinPoint) throws Throwable {
	// // 开启事物
	// Connection connection = jdbcUtil.getConnection();
	// if (!jdbcUtil.isInTransaction()) {
	// jdbcUtil.setThreadLocalConnection(connection);
	// }
	// connection.setAutoCommit(false);
	// LogKit.info(joinPoint.getSignature().getDeclaringTypeName() + "." +
	// joinPoint.getSignature().getName()
	// + "方法开启事务!方法参数为：" + joinPoint.getArgs());
	// }

	@Around("transAction()")
	public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
		Connection connection = null;
		Object result = null;
		try {
			// 开启事物
			connection = jdbcUtil.getConnection();
			if (!jdbcUtil.isInTransaction()) {
				jdbcUtil.bindCurrentConnection(connection);
			}
			// connection.setTransactionIsolation(jdbcUtil.getTransactionLevel());
			connection.setAutoCommit(false);
			loger.info(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName()
					+ "方法开启事务!方法参数为：" + joinPoint.getArgs());
			result = joinPoint.proceed();
			connection.commit();
			loger.info(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName()
					+ "方法事务提交！ ");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			if (connection != null) {
				loger.error("事务回滚 : " + e.getMessage());
				connection.rollback();
			}
			throw new RuntimeException(e.getCause());
		} finally {
			DbUtils.closeQuietly(connection);
			jdbcUtil.removeCurrentConnection();
		}
	}

	public TranstionAspect(JdbcUtil jdbcUtil) {
		super();
		this.jdbcUtil = jdbcUtil;
	}

	// @AfterThrowing(throwing = "ex", pointcut = "transAction()")
	// public void doAfterThrowing(Throwable ex) throws Throwable {
	// LogKit.error("事务回滚 : " + ex.getMessage());
	// Connection connection = null;
	// try {
	// connection = DbKit.getConfig().getConnection();
	// connection.rollback();
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// // DbUtils.closeQuietly(connection);
	// // jdbcUtil.removeCurrentConnection();
	// }
	// }
	//
	// // @AfterReturning注解是在return之前执行
	// @AfterReturning("transAction()")
	// public void doAfterReturning(JoinPoint joinPoint) {
	// Connection connection = null;
	// try {
	// connection = DbKit.getConfig().getConnection();
	// connection.commit();
	// LogKit.info(joinPoint.getSignature().getDeclaringTypeName() + "." +
	// joinPoint.getSignature().getName()
	// + "方法事务提交！ ");
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// // DbUtils.closeQuietly(connection);
	// // jdbcUtil.removeCurrentConnection();
	// }
	// }
}
