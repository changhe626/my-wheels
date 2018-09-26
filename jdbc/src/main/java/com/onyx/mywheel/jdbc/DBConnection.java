package com.onyx.mywheel.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author zk
 * @Description: 获取数据源
 * @date 2018-09-26 15:38
 */
public class DBConnection {

    private static Logger logger = LoggerFactory.getLogger(DBConnection.class);

    /**
     * 储存,加载多个的datasource
     */
    private static List<DruidDataSource> druidDataSources=new ArrayList<DruidDataSource>(5);

    static {
        try{
            InputStream inputStream = DBConnection.class.getClassLoader().getResourceAsStream("jdbc.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            int num = Integer.parseInt(String.valueOf(properties.get("total.datasource")));
            for (int i = 1; i <= num; i++) {
                DruidDataSource dataSource = new DruidDataSource();
                dataSource.setDriverClassName(String.valueOf(properties.get("driver"+i)));
                dataSource.setUrl(String.valueOf(properties.get("url"+i)));
                dataSource.setUsername(String.valueOf(properties.get("username"+i)));
                dataSource.setPassword(String.valueOf(properties.get("password"+i)));
                druidDataSources.add(dataSource);
            }
        }catch (IOException e){
            e.printStackTrace();
            logger.error("没有找到jdbc的配置文件:jdbc.properties");
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取数据源失败了.");
        }
        logger.info("成功获取了数据源");
    }

    /**
     * 获取连接,默认是从第一个数据库中获取连接...
     */
    public synchronized static DruidPooledConnection getConnect(){
        return getConnect(1);
    }


    /**
     * 获取链接
     * @param order  第几个数据库连接池....
     * @return
     */
    public  synchronized static  DruidPooledConnection  getConnect(int order){
        DruidPooledConnection connection=null;
        try {
            connection = druidDataSources.get(order).getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("获取数据库链接失败了:");
        }
        return connection;
    }










}
