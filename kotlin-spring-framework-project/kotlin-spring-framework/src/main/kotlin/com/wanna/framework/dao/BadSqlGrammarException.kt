package com.wanna.framework.dao

import java.sql.SQLException

/**
 * SQL语法错误异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class BadSqlGrammarException(task: String, val sql: String, ex: SQLException?) :
    InvalidDataAccessResourceUsageException("$task; bad SQL grammar [$sql]", ex)