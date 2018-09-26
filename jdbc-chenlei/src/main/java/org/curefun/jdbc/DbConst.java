package org.curefun.jdbc;

/**
 * @author chenlei:
 * @version 创建时间：2018年8月20日 上午11:09:38 类说明
 */
public interface DbConst {
	/**
	 * 数据库类型
	 */
	static final String DATABSE_TYPE_MYSQL = "mysql";
	static final String DATABSE_TYPE_POSTGRE = "postgresql";
	static final String DATABSE_TYPE_ORACLE = "oracle";
	static final String DATABSE_TYPE_SQLSERVER = "sqlserver";
	/**
	 * 分页SQL
	 */
	static final String MYSQL_SQL = "select * from ( {0}) sel_tab00 limit {1},{2}"; // mysql
	static final String POSTGRE_SQL = "select * from ( {0}) sel_tab00 limit {2} offset {1}";// postgresql
	static final String ORACLE_SQL = "select * from (select row_.*,rownum rownum_ from ({0}) row_ where rownum <= {1}) where rownum_>{2}"; // oracle
	static final String SQLSERVER_SQL = "select * from ( select row_number() over(order by tempColumn) tempRowNumber, * from (select top {1} tempColumn = 0, {0}) t ) tt where tempRowNumber > {2}";
}
