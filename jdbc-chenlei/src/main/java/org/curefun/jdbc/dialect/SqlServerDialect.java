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

import org.curefun.jdbc.Record;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

/**
 * SqlServerDialect 为OSC
 * 网友战五渣贡献代码：http://www.oschina.net/question/2333909_234198
 */
public class SqlServerDialect extends Dialect {

	public SqlServerDialect() {

	}

	public String forTableBuilderDoBuild(String tableName) {
		return "select * from " + tableName + " where 1 = 2";
	}

	public String forDbFindById(String tableName, String[] pKeys) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);

		StringBuilder sql = new StringBuilder("select * from ").append(tableName).append(" where ");
		for (int i = 0; i < pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append(pKeys[i]).append(" = ?");
		}
		return sql.toString();
	}

	public String forDbDeleteById(String tableName, String[] pKeys) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);

		StringBuilder sql = new StringBuilder("delete from ").append(tableName).append(" where ");
		for (int i = 0; i < pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append(pKeys[i]).append(" = ?");
		}
		return sql.toString();
	}

	public void forDbSave(String tableName, String[] pKeys, Record record, StringBuilder sql, List<Object> paras) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);

		sql.append("insert into ");
		sql.append(tableName).append('(');
		StringBuilder temp = new StringBuilder();
		temp.append(") values(");

		for (Entry<String, Object> e : record.getColumns().entrySet()) {
			if (paras.size() > 0) {
				sql.append(", ");
				temp.append(", ");
			}
			sql.append(e.getKey());
			temp.append('?');
			paras.add(e.getValue());
		}
		sql.append(temp.toString()).append(')');
	}

	public void forDbUpdate(String tableName, String[] pKeys, Object[] ids, Record record, StringBuilder sql,
                            List<Object> paras) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);

		sql.append("update ").append(tableName).append(" set ");
		for (Entry<String, Object> e : record.getColumns().entrySet()) {
			String colName = e.getKey();
			if (!isPrimaryKey(colName, pKeys)) {
				if (paras.size() > 0) {
					sql.append(", ");
				}
				sql.append(colName).append(" = ? ");
				paras.add(e.getValue());
			}
		}
		sql.append(" where ");
		for (int i = 0; i < pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append(pKeys[i]).append(" = ?");
			paras.add(ids[i]);
		}
	}

	/**
	 * sql.replaceFirst("(?i)select", "") 正则中带有 "(?i)" 前缀，指定在匹配时不区分大小写
	 */
	public String forPaginate(int pageNumber, int pageSize, String findSql) {
		int end = pageNumber * pageSize;
		if (end <= 0) {
			end = pageSize;
		}
		int begin = (pageNumber - 1) * pageSize;
		if (begin < 0) {
			begin = 0;
		}
		StringBuilder ret = new StringBuilder();
		ret.append("SELECT * FROM ( SELECT row_number() over (order by tempcolumn) temprownumber, * FROM ");
		ret.append(" ( SELECT TOP ").append(end).append(" tempcolumn=0,");
		ret.append(findSql.replaceFirst("(?i)select", ""));
		ret.append(")vip)mvp where temprownumber>").append(begin);
		return ret.toString();
	}

	public void fillStatement(PreparedStatement pst, List<Object> paras) throws SQLException {
		fillStatementHandleDateType(pst, paras);
	}

	public void fillStatement(PreparedStatement pst, Object... paras) throws SQLException {
		fillStatementHandleDateType(pst, paras);
	}
}
