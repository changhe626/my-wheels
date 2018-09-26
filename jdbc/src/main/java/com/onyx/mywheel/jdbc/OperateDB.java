package com.onyx.mywheel.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zk
 * @Description: DB的直接操作类
 * @date 2018-09-26 15:15
 */
public class OperateDB {

    private static Logger logger = LoggerFactory.getLogger(DBConnection.class);

    private  Connection connection;

    private OperateDB(){}

    public OperateDB(Connection connection) {
        this.connection = connection;
    }


    /**
     * 查询语句
     * @param sql  sql语句
     * @param params   参数
     * @return   以Map封装参数
     */
    public <T>List<Map<String, Object>> queryRecords(String sql,List<T> params) throws SQLException {
        if(StringUtils.isBlank(sql)){
            throw new IllegalArgumentException("sql语句不能为空");
        }
        QueryRunner runner = new QueryRunner();
        int num = getQuestionNum(sql);
        Object[] objects = list2Array(params, num);
        List<Map<String, Object>> query = new ArrayList<Map<String, Object>>();
        query = runner.query(connection, sql, new MapListHandler(), objects);
        return query;
    }


    /**
     * 查询数量
     * @param sql   sql语句
     * @param params   参数
     * @return   结果条数
     */
    public <T>int queryNum(String sql,List<T> params) throws SQLException {
        if(StringUtils.isBlank(sql)){
            throw new IllegalArgumentException("sql语句不能为空");
        }
        QueryRunner runner = new QueryRunner();
        int num = getQuestionNum(sql);
        Object[] objects = list2Array(params, num);

        Object[] query = runner.query(connection, sql, new ArrayHandler(), objects);
        int nums = Integer.parseInt(String.valueOf(query[0]));
        return nums;
    }


    /**
     * 查询分页的结果
     * @param sql   sql语句
     * @param params  参数
     * @param start  开始条数
     * @param end    结束条数
     * @return
     * @throws SQLException
     */
    public <T>List<Map<String, Object>> queryPageRecords(String sql,List<T> params,int start,int end) throws SQLException {

        StringBuilder sb = new StringBuilder(sql);
        sb.append("  limit  ?,?  ");
        QueryRunner runner = new QueryRunner();
        int num = getQuestionNum(sql);
        Object[] objects = list2Array(params, num);
        Object[] target = new Object[num + 2];
        System.arraycopy(objects,0,target,0,num);
        target[num]=start;
        target[num+1]=end;
        List<Map<String, Object>> query = new ArrayList<Map<String, Object>>();
        query = runner.query(connection, sb.toString(), new MapListHandler(), target);
        return query;
    }





    /**
     * QueryRunner类使用讲解
     　　该类简单化了SQL查询，它与ResultSetHandler组合在一起使用可以完成大部分的数据库操作，能够大大减少编码量。
     　　QueryRunner类提供了两个构造方法：

     默认的构造方法
     需要一个 javax.sql.DataSource 来作参数的构造方法。
     QueryRunner类的主要方法

     public Object query(Connection conn, String sql, Object[] params, ResultSetHandler rsh) throws SQLException：
     执行一个查询操作，在这个查询中，对象数组中的每个元素值被用来作为查询语句的置换参数。该方法会自行处理 PreparedStatement 和 ResultSet 的创建和关闭。
     　　
     public Object query(String sql, Object[] params, ResultSetHandler rsh) throws SQLException:　
     几乎与第一种方法一样；唯一的不同在于它不将数据库连接提供给方法，并且它是从提供给构造方法的数据源(DataSource) 或使用的setDataSource 方法中重新获得 Connection。
     　　
     public Object query(Connection conn, String sql, ResultSetHandler rsh) throws SQLException :
     执行一个不需要置换参数的查询操作。
     　　
     public int update(Connection conn, String sql, Object[] params) throws SQLException:
     用来执行一个更新（插入、更新或删除）操作。
     　　
     public int update(Connection conn, String sql) throws SQLException：
     用来执行一个不需要置换参数的更新操作。
     */

    /**
     * ResultSetHandler接口使用讲解
     　　该接口用于处理java.sql.ResultSet，将数据按要求转换为另一种形式。
     　　ResultSetHandler接口提供了一个单独的方法：Object handle (java.sql.ResultSet .rs)

     ResultSetHandler接口的实现类

     ArrayHandler：把结果集中的第一行数据转成对象数组。
     ArrayListHandler：把结果集中的每一行数据都转成一个数组，再存放到List中。
     BeanHandler：将结果集中的第一行数据封装到一个对应的JavaBean实例中。
     BeanListHandler：将结果集中的每一行数据都封装到一个对应的JavaBean实例中，存放到List里。
     ColumnListHandler：将结果集中某一列的数据存放到List中。
     KeyedHandler(name)：将结果集中的每一行数据都封装到一个Map里，再把这些map再存到一个map里，其key为指定的key。
     MapHandler：将结果集中的第一行数据封装到一个Map里，key是列名，value就是对应的值。
     MapListHandler：将结果集中的每一行数据都封装到一个Map里，然后再存放到List
     */

    /**
     * DbUtils类使用讲解
     　　DbUtils ：提供如关闭连接、装载JDBC驱动程序等常规工作的工具类，里面的所有方法都是静态的。主要方法如下：
     　　
     public static void close(…) throws java.sql.SQLException：　
     DbUtils类提供了三个重载的关闭方法。这些方法检查所提供的参数是不是NULL，如果不是的话，它们就关闭Connection、Statement和ResultSet。
     　　
     public static void closeQuietly(…):
     这一类方法不仅能在Connection、Statement和ResultSet为NULL情况下避免关闭，还能隐藏一些在程序中抛出的SQLEeception。
     　　
     public static void commitAndCloseQuietly(Connection conn)：
     用来提交连接，然后关闭连接，并且在关闭连接时不抛出SQL异常。
     　　
     public static boolean loadDriver(java.lang.String driverClassName)：
     这一方装载并注册JDBC驱动程序，如果成功就返回true。使用该方法，你不需要捕捉这个异常ClassNotFoundException。
     */


    /**
     * 获取问号的数量
     * @param sql  sql语句
     * @return  当前sql中问号的数量
     */
    private static  int getQuestionNum(String sql){
        int num = StringUtils.countMatches(sql, "?");
        return num;
    }


    /**
     * 把一个List转变成为Object[],
     * 只会取出num个list中的元素转变为数组进行返回
     * @param list  list参数
     * @param num list中元素数量
     * @return  object[] 数组
     */
    private static <T>Object[] list2Array(List<T> list,int num){
        if(list==null){
            return new Object[0];
        }
        Object[] objects = new Object[num];
        for (int i = 0; i < num; i++) {
            objects[i]=list.get(i);
        }
        return objects;
    }





}
