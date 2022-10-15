package com.wanna.framework.jdbc.support

import com.wanna.framework.dao.*
import org.slf4j.LoggerFactory
import java.sql.SQLException

/**
 * SqlState的ExceptionTranslator
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class SQLStateSQLExceptionTranslator : AbstractFallbackSQLExceptionTranslator() {
    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(SQLExceptionSubclassTranslator::class.java)

        @JvmStatic
        private val BAD_SQL_GRAMMAR_CODES = HashSet<String>(8)

        @JvmStatic
        private val DATA_INTEGRITY_VIOLATION_CODES = HashSet<String>(8)

        @JvmStatic
        private val DATA_ACCESS_RESOURCE_FAILURE_CODES = HashSet<String>(8)

        @JvmStatic
        private val TRANSIENT_DATA_ACCESS_RESOURCE_CODES = HashSet<String>(8)

        @JvmStatic
        private val CONCURRENCY_FAILURE_CODES = HashSet<String>(4)

        init {
            // 完成SqlState的Code的初始化
            BAD_SQL_GRAMMAR_CODES.add("07")
            BAD_SQL_GRAMMAR_CODES.add("21")
            BAD_SQL_GRAMMAR_CODES.add("2A")
            BAD_SQL_GRAMMAR_CODES.add("37")
            BAD_SQL_GRAMMAR_CODES.add("42")
            BAD_SQL_GRAMMAR_CODES.add("65")
            DATA_INTEGRITY_VIOLATION_CODES.add("01")
            DATA_INTEGRITY_VIOLATION_CODES.add("02")
            DATA_INTEGRITY_VIOLATION_CODES.add("22")
            DATA_INTEGRITY_VIOLATION_CODES.add("23")
            DATA_INTEGRITY_VIOLATION_CODES.add("27")
            DATA_INTEGRITY_VIOLATION_CODES.add("44")
            DATA_ACCESS_RESOURCE_FAILURE_CODES.add("08")
            DATA_ACCESS_RESOURCE_FAILURE_CODES.add("53")
            DATA_ACCESS_RESOURCE_FAILURE_CODES.add("54")
            DATA_ACCESS_RESOURCE_FAILURE_CODES.add("57")
            DATA_ACCESS_RESOURCE_FAILURE_CODES.add("58")
            TRANSIENT_DATA_ACCESS_RESOURCE_CODES.add("JW")
            TRANSIENT_DATA_ACCESS_RESOURCE_CODES.add("JZ")
            TRANSIENT_DATA_ACCESS_RESOURCE_CODES.add("S1")
            CONCURRENCY_FAILURE_CODES.add("40")
            CONCURRENCY_FAILURE_CODES.add("61")
        }
    }

    override fun doTranslate(task: String, sql: String, ex: SQLException): DataAccessException? {
        val sqlState = getSqlState(ex)
        if (sqlState != null && sqlState.length >= 2) {
            val typeCode = sqlState.substring(0, 2)
            if (logger.isDebugEnabled) {
                logger.debug("从SQLSate[$sqlState]当中提取到Code为[$typeCode]")
            }
            if (BAD_SQL_GRAMMAR_CODES.contains(typeCode)) {
                return BadSqlGrammarException(task, sql, ex)
            }
            if (DATA_INTEGRITY_VIOLATION_CODES.contains(typeCode)) {
                return DataIntegrityViolationException(buildMessage(task, sql, ex), ex)
            }
            if (DATA_ACCESS_RESOURCE_FAILURE_CODES.contains(typeCode)) {
                return DataAccessResourceFailureException(buildMessage(task, sql, ex), ex)
            }
            if (TRANSIENT_DATA_ACCESS_RESOURCE_CODES.contains(typeCode)) {
                return TransientDataAccessResourceException(buildMessage(task, sql, ex), ex)
            }
            if (CONCURRENCY_FAILURE_CODES.contains(typeCode)) {
                return ConcurrencyFailureException(buildMessage(task, sql, ex), ex)
            }
        }
        return if (ex.javaClass.name.contains("Timeout")) QueryTimeoutException(
            buildMessage(task, sql, ex),
            ex
        ) else null
    }

    private fun getSqlState(ex: SQLException): String? {
        var sqlState = ex.sqlState
        if (sqlState == null) {
            val nestedEx = ex.nextException
            if (nestedEx != null) {
                sqlState = nestedEx.sqlState
            }
        }
        return sqlState
    }
}