package org.curefun.jdbc;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;
import org.curefun.jdbc.dialect.*;
import org.curefun.jdbc.util.BeanConverter;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author chenlei:
 * @version 创建时间：2018年5月26日 下午4:07:35 类说明
 */
public class JdbcUtil {
	// private DataSource dataSource;
	private Logger loger = Logger.getLogger(getClass());
	private ThreadLocal<Connection> con_threadLocal = new ThreadLocal<Connection>();
	private QueryRunner queryRunner;
	private Dialect dialect;
	private String dbType = DbConst.MYSQL_SQL;
	private int transactionlevel = Connection.TRANSACTION_REPEATABLE_READ;

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public int getTransactionlevel() {
		return transactionlevel;
	}

	public void setTransactionlevel(int transactionlevel) {
		this.transactionlevel = transactionlevel;
	}

	private boolean isSysClass(Class<?> clz) {
		try {
			return clz != null && clz.getClassLoader() == null;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public QueryRunner getQueryRunner() {
		return queryRunner;
	}

	public JdbcUtil(DataSource dataSource) {
		this(dataSource, DbConst.DATABSE_TYPE_MYSQL);
	}

	public JdbcUtil(DataSource dataSource, String dbType) {
		this.dbType = dbType;
		queryRunner = new QueryRunner(dataSource);
		switch (dbType) {
		case DbConst.DATABSE_TYPE_MYSQL:
			dialect = new MysqlDialect();
			break;
		case DbConst.DATABSE_TYPE_ORACLE:
			dialect = new OracleDialect();
			break;
		case DbConst.DATABSE_TYPE_POSTGRE:
			dialect = new PostgreSqlDialect();
			break;
		case DbConst.DATABSE_TYPE_SQLSERVER:
			dialect = new SqlServerDialect();
			break;
		default:
			dialect = new MysqlDialect();
			break;
		}
		// queryRunner.update(sql, param)
	}

	public void removeCurrentConnection() {
		con_threadLocal.remove();
	}

	public void bindCurrentConnection(Connection connection) {
		con_threadLocal.set(connection);
	}

	public boolean isInTransaction() {
		return con_threadLocal.get() != null;
	}

	public synchronized Connection getConnection() throws SQLException {
		Connection connection = con_threadLocal.get();
		if (connection == null) {
			connection = this.queryRunner.getDataSource().getConnection();
			// con_threadLocal.set(connection);
		}
		return connection;
	}

	/**
	 * 这个关闭方法只会关闭是自动提交状态的连接，非自动提交状态的不会关闭
	 * 
	 * @throws SQLException
	 */
	public void closeConnection() throws SQLException {
		Connection connection = con_threadLocal.get();
		if (connection != null) {
			// 如果是自动提交状态直接关闭连接(mysql,sqlserver均是默认是自动提交的，oracle可设置)
			if (connection.getAutoCommit()) {
				connection.close();
				this.con_threadLocal.remove();
			}
		}
	}

	public int executeNonQuery(String sql, Object... params) throws SQLException {
		loger.info(String.format("sql:%s,params:%s", sql, Arrays.toString(params)));
		int result = 0;
		try {
			result = this.queryRunner.update(this.getConnection(), sql, params);
			return result;
		} catch (Exception e) {
			throw new SQLException(e);
		} finally {
			// 这个关闭方法只会关闭是自动提交状态的连接，非自动提交状态的不会关闭
			this.closeConnection();
		}

	}

	public <T> boolean insertByBean(String tableName, String primaryKey, T bean, Class<? extends T> classz)
			throws Exception {
		Map<String, Object> map = BeanConverter.bean2map(bean, classz);
		return insertByRecord(tableName, primaryKey, new Record().setColumns(map));
	}

	public boolean insertByRecord(String tableName, String primaryKey, Record record) throws SQLException {
		String[] pKeys = primaryKey.split(",");
		List<Object> paras = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		this.dialect.forDbSave(tableName, pKeys, record, sql, paras);
		PreparedStatement pst = null;
		try {
			Connection conn = this.getConnection();
			if (dbType.equals(DbConst.DATABSE_TYPE_ORACLE)) {
				pst = conn.prepareStatement(sql.toString(), pKeys);
			} else {
				pst = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
			}
			fillStatementHandleDateType(pst, paras);
			int result = pst.executeUpdate();
			this.dialect.getRecordGeneratedKey(pst, record, pKeys);
			return result >= 1;
		} catch (Exception e) {
			throw new SQLException(e);
		} finally {
			DbUtils.closeQuietly(pst);
			this.closeConnection();
		}

	}

	public boolean updateByRecord(String tableName, String primaryKey, Record record) throws Exception {
		String[] pKeys = primaryKey.split(",");
		Object[] ids = new Object[pKeys.length];

		for (int i = 0; i < pKeys.length; i++) {
			ids[i] = record.get(pKeys[i].trim()); // .trim() is important!
			if (ids[i] == null)
				throw new SQLException(
						"You can't update record without Primary Key, " + pKeys[i] + " can not be null.");
		}
		StringBuilder sql = new StringBuilder();
		List<Object> paras = new ArrayList<Object>();
		this.dialect.forDbUpdate(tableName, pKeys, ids, record, sql, paras);
		return this.executeNonQuery(sql.toString(), paras.toArray()) >= 1;
	}

	public Page<Record> paginateRecord(int pageNumber, int pageSize, String sql, Object... params) throws SQLException {
		String countSql = "select count(*) from (" + sql + ") tab00";
		String pageSql = dialect.forPaginate(pageNumber, pageSize, sql);
		List<Record> records = queryForRecords(pageSql, params);
		Object rowsCount = queryOne(countSql, Object.class, params);
		int totalCount = rowsCount == null ? 0 : Integer.parseInt(rowsCount.toString());
		int totalPage = (int) (totalCount / pageSize);
		return new Page<>(records, pageNumber, pageSize, totalPage, totalCount);
	}

	public <T> Page<T> paginate(int pageNumber, int pageSize, String sql, Class<T> classz, Object... params)
			throws SQLException {
		String countSql = "select count(*) from (" + sql + ") tab00";
		String pageSql = dialect.forPaginate(pageNumber, pageSize, sql);
		List<T> records = queryForList(pageSql, classz, params);
		Object rowsCount = queryOne(countSql, Object.class, params);
		int totalCount = rowsCount == null ? 0 : Integer.parseInt(rowsCount.toString());
		int totalPage = (int) (totalCount / pageSize);
		return new Page<>(records, pageNumber, pageSize, totalPage, totalCount);
	}

	public void batchExecute(String sql, Object[][] params) throws SQLException {
		loger.info(String.format("sql:%s,params:%s", sql, params.toString()));
		try {
			this.queryRunner.batch(this.getConnection(), sql, params);
		} catch (Exception e) {
			throw new SQLException(e);
		} finally {
			this.closeConnection();
		}

	}

	public List<Record> queryForRecords(String sql, Object... params) throws SQLException {
		loger.info(String.format("sql:%s,params:%s", sql, Arrays.toString(params)));
		Connection conn = null;
		try {
			conn = this.getConnection();
			PreparedStatement pst = conn.prepareStatement(sql);
			this.fillStatementHandleDateType(pst, params);
			ResultSet rs = pst.executeQuery();
			List<Record> result = RecordBuilder.me.build(rs); // RecordBuilder.build(config, rs);
			return result;
		} catch (Exception e) {
			throw new SQLException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}

	}

	public Record queryRecord(String sql, Object... params) throws SQLException {
		loger.info(String.format("sql:%s,params:%s", sql, Arrays.toString(params)));
		List<Record> records = queryForRecords(sql, params);
		return records.size() > 0 ? records.get(0) : null;

	}

	public List<Map<String, Object>> queryForMapList(String sql, Object... params) throws SQLException {
		loger.info(String.format("sql:%s,params:%s", sql, Arrays.toString(params)));
		return queryRunner.query(sql, new MapListHandler(), params);
	}

	public Map<String, Object> queryForMap(String sql, Object... params) throws SQLException {
		loger.info(String.format("sql:%s,params:%s", sql, Arrays.toString(params)));
		return queryRunner.query(sql, new MapHandler(), params);
	}

	public <T> T queryOne(String sql, Class<? extends T> classType, Object... params) throws SQLException {
		loger.info(String.format("sql:%s,params:%s", sql, Arrays.toString(params)));
		// 解决查询单列值报错的bug
		if (isSysClass(classType)) {
			T t = this.queryRunner.query(sql, new ResultSetHandler<T>() {
				public T handle(ResultSet rs) throws SQLException {
					if (rs.next())
						return (T) rs.getObject(1);
					else
						return null;
				}

			}, params);
			return t;
		}
		return queryRunner.query(sql, new BeanHandler<T>(classType), params);
	}

	public <T> List<T> queryForList(String sql, Class<? extends T> classType, Object... params) throws SQLException {
		loger.info(String.format("sql:%s,params:%s", sql, Arrays.toString(params)));
		if (isSysClass(classType)) {
			List<T> list = (List<T>) this.queryRunner.query(sql, new SclarListHander<T>(), params);
			return list;
		}
		return this.queryRunner.query(sql, new BeanListHandler<T>(classType), params);
	}

	static class SclarListHander<T> implements ResultSetHandler<List<T>> {
		public List<T> handle(ResultSet rs) throws SQLException {
			List<T> result = new ArrayList<T>();
			while (rs.next()) {
				result.add((T) rs.getObject(1));
			}
			return result;
		}

	}

	/**
	 * fillStatement 时处理日期类型
	 */
	private void fillStatementHandleDateType(PreparedStatement pst, List<Object> paras) throws SQLException {
		for (int i = 0, size = paras.size(); i < size; i++) {
			Object value = paras.get(i);
			if (value instanceof java.util.Date) {
				if (value instanceof java.sql.Date) {
					pst.setDate(i + 1, (java.sql.Date) value);
				} else if (value instanceof java.sql.Timestamp) {
					pst.setTimestamp(i + 1, (java.sql.Timestamp) value);
				} else {
					// Oracle、SqlServer 中的 TIMESTAMP、DATE 支持 new Date() 给值
					java.util.Date d = (java.util.Date) value;
					pst.setTimestamp(i + 1, new java.sql.Timestamp(d.getTime()));
				}
			} else {
				pst.setObject(i + 1, value);
			}
		}
	}

	/**
	 * fillStatement 时处理日期类型
	 */
	private void fillStatementHandleDateType(PreparedStatement pst, Object... paras) throws SQLException {
		for (int i = 0; i < paras.length; i++) {
			Object value = paras[i];
			if (value instanceof java.util.Date) {
				if (value instanceof java.sql.Date) {
					pst.setDate(i + 1, (java.sql.Date) value);
				} else if (value instanceof java.sql.Timestamp) {
					pst.setTimestamp(i + 1, (java.sql.Timestamp) value);
				} else {
					// Oracle、SqlServer 中的 TIMESTAMP、DATE 支持 new Date() 给值
					java.util.Date d = (java.util.Date) value;
					pst.setTimestamp(i + 1, new java.sql.Timestamp(d.getTime()));
				}
			} else {
				pst.setObject(i + 1, value);
			}
		}
	}

	/**
	 * 用于获取 Db.save(tableName, record) 以后自动生成的主键值，可通过覆盖此方法实现更精细的控制 目前只有
	 * PostgreSqlDialect，覆盖过此方法
	 */
	public void getRecordGeneratedKey(PreparedStatement pst, Record record, String[] pKeys) throws SQLException {
		ResultSet rs = pst.getGeneratedKeys();
		for (String pKey : pKeys) {
			if (record.get(pKey) == null || dbType.equals(DbConst.DATABSE_TYPE_ORACLE)) {
				if (rs.next()) {
					record.set(pKey, rs.getObject(1)); // It returns Long for int colType for mysql
				}
			}
		}
		rs.close();
	}
}
