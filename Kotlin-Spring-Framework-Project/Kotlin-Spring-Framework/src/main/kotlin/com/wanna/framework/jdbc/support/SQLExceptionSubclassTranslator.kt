package com.wanna.framework.jdbc.support

import com.wanna.framework.dao.*
import java.sql.*

/**
 * SQLException的子类翻译器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class SQLExceptionSubclassTranslator : AbstractFallbackSQLExceptionTranslator() {

    init {
        // 设置Fallback的SQLExceptionTranslator为基于SQLState去进行判断的Translator
        this.fallbackTranslator = SQLStateSQLExceptionTranslator()
    }

    override fun doTranslate(task: String, sql: String, ex: SQLException): DataAccessException? {
        if (ex is SQLTransientException) {
            // 获取SQLConnection失败
            if (ex is SQLTransientConnectionException) {
                return TransientDataAccessResourceException(buildMessage(task, sql, ex), ex)
            }

            // SQL事务回滚失败
            if (ex is SQLTransactionRollbackException) {
                return ConcurrencyFailureException(buildMessage(task, sql, ex), ex)
            }

            // SQL超时
            if (ex is SQLTimeoutException) {
                return QueryTimeoutException(buildMessage(task, sql, ex), ex)
            }
        } else if (ex is SQLNonTransientException) {
            if (ex is SQLNonTransientConnectionException) {
                return DataAccessResourceFailureException(buildMessage(task, sql, ex))
            }
            if (ex is SQLDataException) {
                return DataIntegrityViolationException(buildMessage(task, sql, ex))
            }
            // 违反数据库约束条件
            if (ex is SQLIntegrityConstraintViolationException) {
                return DataIntegrityViolationException(buildMessage(task, sql, ex))
            }
            // SQL授权失败
            if (ex is SQLInvalidAuthorizationSpecException) {
                return PermissionDeniedDataAccessException(buildMessage(task, sql, ex), ex)
            }

            // SQL语法错误
            if (ex is SQLSyntaxErrorException) {
                return BadSqlGrammarException(task, sql, ex)
            }
            // SQL特征不支持
            if (ex is SQLFeatureNotSupportedException) {
                return InvalidDataAccessApiUsageException(buildMessage(task, sql, ex), ex)
            }
        }
        return null
    }
}