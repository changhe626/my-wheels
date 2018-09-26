/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.curefun.jdbc.dialect;

import org.curefun.jdbc.Page;
import org.curefun.jdbc.Record;
import org.curefun.jdbc.RecordBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Dialect.
 */
public abstract class Dialect {

	// 指示 Generator、ModelBuilder、RecordBuilder 是否保持住 Byte、Short 类型
	protected boolean keepByteAndShort = false;
	protected org.curefun.jdbc.RecordBuilder recordBuilder = RecordBuilder.me;

	// Methods for common
	public abstract String forTableBuilderDoBuild(String tableName);

	public abstract String forPaginate(int pageNumber, int pageSize, String findSql);

	// Methods for DbPro. Do not delete the String[] pKeys parameter, the element of
	// pKeys needs to trim()
	public abstract String forDbFindById(String tableName, String[] pKeys);

	public abstract String forDbDeleteById(String tableName, String[] pKeys);

	public abstract void forDbSave(String tableName, String[] pKeys, Record record, StringBuilder sql,
			List<Object> paras);

	public abstract void forDbUpdate(String tableName, String[] pKeys, Object[] ids, Record record, StringBuilder sql,
			List<Object> paras);

	/**
	 * 指示 MetaBuilder 生成的 ColumnMeta.javaType 是否保持住 Byte、Short 类型 进而
	 * BaseModelBuilder 生成针对 Byte、Short 类型的获取方法： getByte(String)、getShort(String)
	 */
	public boolean isKeepByteAndShort() {
		return keepByteAndShort;
	}

	/**
	 * 配置自定义 RecordBuilder
	 * 
	 * 通过继承扩展 RecordBuilder 可以对 JDBC 到 java 数据类型进行定制化转换 不同数据库从 JDBC 到 java
	 * 数据类型的映射关系有所不同
	 * 
	 * 此外，还可以通过改变 RecordBuilder.buildLabelNamesAndTypes() 方法逻辑，实现下划线字段名转驼峰变量名的功能
	 */
	public Dialect setRecordBuilder(RecordBuilder recordBuilder) {
		this.recordBuilder = recordBuilder;
		return this;
	}

	/**
	 * 用于获取 Db.save(tableName, record) 以后自动生成的主键值，可通过覆盖此方法实现更精细的控制 目前只有
	 * PostgreSqlDialect，覆盖过此方法
	 */
	public void getRecordGeneratedKey(PreparedStatement pst, Record record, String[] pKeys) throws SQLException {
		ResultSet rs = pst.getGeneratedKeys();
		for (String pKey : pKeys) {
			if (record.get(pKey) == null || isOracle()) {
				if (rs.next()) {
					record.set(pKey, rs.getObject(1)); // It returns Long for int colType for mysql
				}
			}
		}
		rs.close();
	}

	public boolean isOracle() {
		return false;
	}

	public boolean isTakeOverDbPaginate() {
		return false;
	}

	public Page<Record> takeOverDbPaginate(Connection conn, int pageNumber, int pageSize, Boolean isGroupBySql,
			String totalRowSql, StringBuilder findSql, Object... paras) throws SQLException {
		throw new RuntimeException("You should implements this method in " + getClass().getName());
	}

	public boolean isTakeOverModelPaginate() {
		return false;
	}

	public void fillStatement(PreparedStatement pst, List<Object> paras) throws SQLException {
		for (int i = 0, size = paras.size(); i < size; i++) {
			pst.setObject(i + 1, paras.get(i));
		}
	}

	public void fillStatement(PreparedStatement pst, Object... paras) throws SQLException {
		for (int i = 0; i < paras.length; i++) {
			pst.setObject(i + 1, paras[i]);
		}
	}

	public String getDefaultPrimaryKey() {
		return "id";
	}

	public boolean isPrimaryKey(String colName, String[] pKeys) {
		for (String pKey : pKeys) {
			if (colName.equalsIgnoreCase(pKey)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 一、forDbXxx 系列方法中若有如下两种情况之一，则需要调用此方法对 pKeys 数组进行 trim(): 1：方法中调用了
	 * isPrimaryKey(...)：为了防止在主键相同情况下，由于前后空串造成 isPrimaryKey 返回 false 2：为了防止
	 * tableName、colName 与数据库保留字冲突的，添加了包裹字符的：为了防止串包裹区内存在空串 如 mysql 使用的 "`" 字符以及
	 * PostgreSql 使用的 "\"" 字符 不满足以上两个条件之一的 forDbXxx 系列方法也可以使用 trimPrimaryKeys(...)
	 * 方法让 sql 更加美观，但不是必须
	 * 
	 * 二、forModelXxx 由于在映射时已经trim()，故不再需要调用此方法
	 */
	public void trimPrimaryKeys(String[] pKeys) {
		for (int i = 0; i < pKeys.length; i++) {
			pKeys[i] = pKeys[i].trim();
		}
	}

	protected static class Holder {
		// "order\\s+by\\s+[^,\\s]+(\\s+asc|\\s+desc)?(\\s*,\\s*[^,\\s]+(\\s+asc|\\s+desc)?)*";
		private static final Pattern ORDER_BY_PATTERN = Pattern.compile(
				"order\\s+by\\s+[^,\\s]+(\\s+asc|\\s+desc)?(\\s*,\\s*[^,\\s]+(\\s+asc|\\s+desc)?)*",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	}

	public String replaceOrderBy(String sql) {
		return Holder.ORDER_BY_PATTERN.matcher(sql).replaceAll("");
	}

	/**
	 * fillStatement 时处理日期类型
	 */
	protected void fillStatementHandleDateType(PreparedStatement pst, List<Object> paras) throws SQLException {
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
	protected void fillStatementHandleDateType(PreparedStatement pst, Object... paras) throws SQLException {
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

	protected byte[] handleBlob(Blob blob) throws SQLException {
		if (blob == null)
			return null;

		InputStream is = null;
		try {
			is = blob.getBinaryStream();
			if (is == null)
				return null;
			byte[] data = new byte[(int) blob.length()]; // byte[] data = new byte[is.available()];
			if (data.length == 0)
				return null;
			is.read(data);
			return data;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}

	protected String handleClob(Clob clob) throws SQLException {
		if (clob == null)
			return null;

		Reader reader = null;
		try {
			reader = clob.getCharacterStream();
			if (reader == null)
				return null;
			char[] buffer = new char[(int) clob.length()];
			if (buffer.length == 0)
				return null;
			reader.read(buffer);
			return new String(buffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}
}
