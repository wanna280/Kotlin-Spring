package com.wanna.framework.jdbc.support

import com.wanna.framework.dao.DataAccessException
import java.sql.SQLException
import javax.sql.DataSource

/**
 * SQL的ErrorCode的翻译器，将SQL的ErrorCode去翻译成为正确的[DataAccessException]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class SQLErrorCodeSQLExceptionTranslator(val dataSource: DataSource) : AbstractFallbackSQLExceptionTranslator() {

    private val errorCodes = SQLErrorCodesFactory.getErrorCodes(dataSource)

    init {
        // 初始化Fallback的SQLExceptionTranslator
        this.fallbackTranslator = SQLExceptionSubclassTranslator()
    }

    override fun doTranslate(task: String, sql: String, ex: SQLException): DataAccessException? {
        return null  // TODO
    }
}