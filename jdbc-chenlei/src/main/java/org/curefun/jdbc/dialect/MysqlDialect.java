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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map.Entry;

/**
 * MysqlDialect.
 */
public class MysqlDialect extends Dialect {
	static final String MYSQL_SQL = "select * from ( {0}) sel_tab00 limit {1},{2}"; // mysql

	public String forTableBuilderDoBuild(String tableName) {
		return "select * from `" + tableName + "` where 1 = 2";
	}

	public String forDbFindById(String tableName, String[] pKeys) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);

		StringBuilder sql = new StringBuilder("select * from `").append(tableName).append("` where ");
		for (int i = 0; i < pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append('`').append(pKeys[i]).append("` = ?");
		}
		return sql.toString();
	}

	public String forDbDeleteById(String tableName, String[] pKeys) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);

		StringBuilder sql = new StringBuilder("delete from `").append(tableName).append("` where ");
		for (int i = 0; i < pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append('`').append(pKeys[i]).append("` = ?");
		}
		return sql.toString();
	}

	/**
	 * Do not delete the String[] pKeys parameter, the element of pKeys needs to
	 * trim()
	 */
	public void forDbSave(String tableName, String[] pKeys, Record record, StringBuilder sql, List<Object> paras) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys); // important

		sql.append("insert into `");
		sql.append(tableName).append("`(");
		StringBuilder temp = new StringBuilder();
		temp.append(") values(");

		for (Entry<String, Object> e : record.getColumns().entrySet()) {
			if (paras.size() > 0) {
				sql.append(", ");
				temp.append(", ");
			}
			sql.append('`').append(e.getKey()).append('`');
			temp.append('?');
			paras.add(e.getValue());
		}
		sql.append(temp.toString()).append(')');
	}

	public void forDbUpdate(String tableName, String[] pKeys, Object[] ids, Record record, StringBuilder sql,
                            List<Object> paras) {
		tableName = tableName.trim();
		trimPrimaryKeys(pKeys);

		sql.append("update `").append(tableName).append("` set ");
		for (Entry<String, Object> e : record.getColumns().entrySet()) {
			String colName = e.getKey();
			if (!isPrimaryKey(colName, pKeys)) {
				if (paras.size() > 0) {
					sql.append(", ");
				}
				sql.append('`').append(colName).append("` = ? ");
				paras.add(e.getValue());
			}
		}
		sql.append(" where ");
		for (int i = 0; i < pKeys.length; i++) {
			if (i > 0) {
				sql.append(" and ");
			}
			sql.append('`').append(pKeys[i]).append("` = ?");
			paras.add(ids[i]);
		}
	}

	public String forPaginate(int pageNumber, int pageSize, String findSql) {
		int beginNum = (pageNumber - 1) * pageSize;
		String[] sqlParam = new String[3];
		sqlParam[0] = findSql;
		sqlParam[1] = beginNum + "";
		sqlParam[2] = pageSize + "";
		return findSql = MessageFormat.format(MYSQL_SQL, sqlParam);
	}
}
