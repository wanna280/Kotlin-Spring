package com.wanna.framework.jdbc.support

import com.wanna.framework.dao.DataAccessException
import java.sql.SQLException
import javax.sql.DataSource

/**
 * SQL的ErrorCode的翻译器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
class SQLErrorCodeSQLExceptionTranslator(val dataSource: DataSource) : AbstractFallbackSQLExceptionTranslator() {

    override fun doTranslate(task: String, sql: String, ex: SQLException): DataAccessException? {

        return null  // TODO
    }
}