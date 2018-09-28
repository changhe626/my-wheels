package com.onyx.mywheel.jdbc;

import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zk
 * @Description:
 * @date 2018-09-26 16:50
 */
public class Test {

    public static void main(String[] args) throws SQLException {
        DruidPooledConnection connect = DBConnection.getConnect();
        OperateDB db = new OperateDB(connect);
        List<Integer> collect = Stream.of(1).collect(Collectors.toList());
        List<Map<String, Object>> query = db.queryRecords("select * from t0_dic_crowd  where  crowd_sex=?", collect);
        for (Map<String, Object> map : query) {
            System.out.println(map);
        }

        int queryNum = db.queryNum("select count(*) from t0_dic_crowd  where  crowd_sex=?", collect);
        System.out.println(queryNum);

        List<Map<String, Object>> query2 = db.queryPageRecords("select * from t0_dic_crowd  where  crowd_sex=?", collect,1,4);
        for (Map<String, Object> map : query2) {
            System.out.println(map);
        }


        /*TransactionalManager manager = new TransactionalManager(db2);

        List<String> collect1 = Stream.of("开场白11", "20180830090000001").collect(Collectors.toList());
        int record = db2.updateRecord("UPDATE  basic_option_info  set option_name=?  where  option_id=? ", collect1);
        System.out.println(record);*/

        int i=1/0;


    }





}
