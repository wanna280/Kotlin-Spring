package com.wanna.mybatis.spring

import com.wanna.framework.dao.DataAccessException
import com.wanna.framework.dao.support.PersistenceExceptionTranslator
import com.wanna.framework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
import com.wanna.framework.jdbc.support.SQLExceptionTranslator
import com.wanna.framework.transaction.TransactionException
import org.apache.ibatis.exceptions.PersistenceException
import java.sql.SQLException
import javax.sql.DataSource

/**
 * MyBatis的持久层异常翻译器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 * @param dataSource dataSource
 * @param exceptionTranslatorLazyInit SQLExceptionTranslator是否需要懒加载?
 */
open class MyBatisExceptionTranslator
    (private val dataSource: DataSource, exceptionTranslatorLazyInit: Boolean = true) :
    PersistenceExceptionTranslator {

    /**
     * SQLException的翻译器, 提供将SQLException翻译为DataAccessException
     */
    private var exceptionTranslator: SQLExceptionTranslator? = null

    init {
        // 如果不是懒加载的话, 在这里去完成初始化工作
        if (!exceptionTranslatorLazyInit) {
            initExceptionTranslator()
        }
    }


    /**
     * 将给定的RuntimeException异常翻译成为Spring统一的[DataAccessException]
     *
     * @param ex 待翻译的异常
     * @return 翻译得到的DataAccessException(支持去翻译[PersistenceException], 别的类型不支持翻译, return null)
     */
    override fun translateExceptionIfPossible(ex: RuntimeException): DataAccessException? {
        if (ex is PersistenceException) {
            var exToTranslate = ex
            if (ex.cause is PersistenceException) {
                exToTranslate = ex.cause as PersistenceException
            }
            return when (exToTranslate.cause) {
                // 如果cause是SQLException, 那么翻译
                is SQLException -> {
                    // 先初始化SQLExceptionTranslator
                    initExceptionTranslator()

                    // 执行异常的翻译, 第二个参数是sql, MyBatis传递是一个null, Emmmm
                    exceptionTranslator?.translate(exToTranslate.message, null, exToTranslate.cause as SQLException)
                }
                // 如果cause是TransactionException, 那么丢出去异常
                is TransactionException -> throw (exToTranslate.cause as TransactionException)
                // 其他情况, 使用MyBatisSystemException去包装一层
                else -> MyBatisSystemException(ex)
            }
        }
        return null
    }

    /**
     * 初始化ExceptionTranslator, 我们这里创建一个[SQLErrorCodeSQLExceptionTranslator]
     */
    @Synchronized
    private fun initExceptionTranslator() {
        if (this.exceptionTranslator == null) {
            this.exceptionTranslator = SQLErrorCodeSQLExceptionTranslator(dataSource)
        }
    }
}