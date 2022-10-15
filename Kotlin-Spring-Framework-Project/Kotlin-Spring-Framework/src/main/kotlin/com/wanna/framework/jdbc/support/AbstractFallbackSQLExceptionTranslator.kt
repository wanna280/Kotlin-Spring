package com.wanna.framework.jdbc.support

import com.wanna.framework.dao.DataAccessException
import java.sql.SQLException

/**
 * 支持使用Fallback的SQLExceptionTranslator
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 * @see SQLExceptionTranslator
 */
abstract class AbstractFallbackSQLExceptionTranslator : SQLExceptionTranslator {

    /**
     * Fallback的SQLExceptionTranslator
     */
    var fallbackTranslator: SQLExceptionTranslator? = null

    override fun translate(task: String?, sql: String?, ex: SQLException?): DataAccessException? {
        ex ?: throw IllegalStateException("无法翻译一个空的SQLException")
        val taskToUse = task ?: ""
        val sqlToUse = sql ?: ""
        var dataAccessException = doTranslate(taskToUse, sqlToUse, ex)
        if (dataAccessException != null) {
            return dataAccessException
        }
        dataAccessException = fallbackTranslator?.translate(taskToUse, sqlToUse, ex)
        if (dataAccessException != null) {
            return dataAccessException
        }

        // 丢出无法识别的SQLException
        return UncategorizedSQLException(taskToUse, sqlToUse, ex)
    }

    /**
     * 执行真正的翻译(将[SQLException]翻译成为[DataAccessException])，具体逻辑交给子类去进行实现
     *
     * @param task task
     * @param sql sql
     * @param ex SQLException
     * @return DataAccessException
     */
    protected abstract fun doTranslate(task: String, sql: String, ex: SQLException): DataAccessException?

    /**
     * 构建异常消息的Message
     *
     * @param task task
     * @param sql sql
     * @param ex SQLException
     * @return error message
     */
    protected open fun buildMessage(task: String, sql: String, ex: SQLException): String =
        "task; SQL [$sql]; ${ex.message}"
}