package com.onyx.mywheel.jdbc;

import org.apache.commons.dbutils.DbUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;


/**
 * @author zk
 * @Description:  事物的管理
 * @date 2018-09-27 9:55
 */
@Aspect
public class TransactionalManager {

    private static Logger logger = LoggerFactory.getLogger(TransactionalManager.class);

    private OperateDB operateDB;


    @Pointcut("@annotation(com.onyx.mywheel.jdbc.Transactional)")
    private void pointCut(){}


    @Around("pointCut()")
    public Object managerTransactional(ProceedingJoinPoint joinPoint) throws Throwable {
        Connection connection=null;
        Object result = null;
        try {
            connection = operateDB.getConnection();
            connection.setAutoCommit(false);
            logger.info(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName()+"方法开启事务!方法参数为：" + joinPoint.getArgs());
            result=joinPoint.proceed();
            connection.commit();
            logger.info(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName()+"方法事务提交！ ");
            return result;
        }catch (Exception e){
            e.printStackTrace();
            if (connection != null) {
                logger.error("事务回滚 : " + e.getMessage());
                connection.rollback();
            }
            throw new RuntimeException(e.getCause());
        }finally {
            DbUtils.closeQuietly(connection);
        }
    }



    public TransactionalManager(OperateDB operateDB) {
        this.operateDB = operateDB;
    }

}
