package com.wanna.framework.dao.support

import com.wanna.framework.dao.DataAccessException

/**
 * 持久层异常的翻译器, 将一个普通的异常去翻译成为[DataAccessException]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 * @see DataAccessException
 */
interface PersistenceExceptionTranslator {

    /**
     * 供持久层框架去将给定的[RuntimeException]去翻译成为Spring家通用的[DataAccessException].
     * 不要翻译那些不被理解的异常, 例如它是来自于别的持久层框架, 或者它是由用户产生的异常, 和持久层无关
     *
     * 实现方可能还会使用Spring JDBC当中的提供的异常翻译器, 在rootCause是[java.sql.SQLException]的情况下可以去提供更多的信息
     *
     * @param ex 待翻译的RuntimeException
     * @return 翻译之后得到的[DataAccessException], 翻译失败return null
     * @see com.wanna.framework.dao.DataIntegrityViolationException
     * @see com.wanna.framework.jdbc.support.SQLExceptionTranslator
     */
    fun translateExceptionIfPossible(ex: RuntimeException): DataAccessException?
}