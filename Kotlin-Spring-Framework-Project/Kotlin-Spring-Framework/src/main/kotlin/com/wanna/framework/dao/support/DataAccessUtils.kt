package com.wanna.framework.dao.support

/**
 * 数据访问相关的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 * @see com.wanna.framework.dao.DataAccessException
 */
object DataAccessUtils {

    /**
     * 如果必要的话，使用给定的[PersistenceExceptionTranslator]去将异常翻译成为[com.wanna.framework.dao.DataAccessException]
     *
     * @param rawException 原本的RuntimeException
     * @param exceptionTranslator PersistenceExceptionTranslator
     * @return 如果翻译成功，那么返回DataAccessException；翻译失败，返回rawException
     */
    @JvmStatic
    fun translateIfNecessary(
        rawException: RuntimeException,
        exceptionTranslator: PersistenceExceptionTranslator
    ): RuntimeException {
        val accessException = exceptionTranslator.translateExceptionIfPossible(rawException)
        return accessException ?: rawException
    }
}