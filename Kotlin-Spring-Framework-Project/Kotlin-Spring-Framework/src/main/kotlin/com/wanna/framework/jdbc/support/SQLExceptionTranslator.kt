package com.wanna.framework.jdbc.support

import com.wanna.framework.dao.DataAccessException
import java.sql.SQLException

/**
 *
 * [SQLException]的翻译器，提供将JDBC的[SQLException]去翻译成为Spring的[DataAccessException]的相关功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 */
interface SQLExceptionTranslator {

    /**
     * 将JDBC的[SQLException]去翻译成为Spring通用的[DataAccessException]
     *
     * @param task
     * @param sql sql
     * @param ex SQLException
     * @return DataAccessException
     */
    fun translate(task: String?, sql: String?, ex: SQLException?): DataAccessException?
}