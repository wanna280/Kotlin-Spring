package com.wanna.framework.jdbc

import com.wanna.framework.dao.DataAccessResourceFailureException
import java.sql.SQLException

/**
 * 无法获取一条JDBC连接的异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class CannotGetJdbcConnectionException(message: String?, cause: SQLException) :
    DataAccessResourceFailureException(message, cause)