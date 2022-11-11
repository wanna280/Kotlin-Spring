package com.wanna.framework.jdbc.support

import com.wanna.framework.dao.UncategorizedDataAccessException
import java.sql.SQLException

/**
 * 无法识别的[SQLException]，实现了[UncategorizedDataAccessException]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 */
open class UncategorizedSQLException(task: String, val sql: String, cause: SQLException) :
    UncategorizedDataAccessException(
        "task; 无法识别的SQLException, SQL=[$sql]; SQL state=[${cause.sqlState}]; errorCode=[${cause.errorCode}]",
        cause
    ) {
    open fun getSQLException(): SQLException = cause as SQLException
}