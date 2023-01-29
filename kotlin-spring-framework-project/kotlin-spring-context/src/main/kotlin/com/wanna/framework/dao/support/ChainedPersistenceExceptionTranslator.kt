package com.wanna.framework.dao.support

import com.wanna.framework.dao.DataAccessException
import java.util.*

/**
 * 提供了链式的[PersistenceExceptionTranslator]去进行翻译的功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class ChainedPersistenceExceptionTranslator : PersistenceExceptionTranslator {

    /**
     * 内部委托的持久层异常翻译器列表[PersistenceExceptionTranslator]
     */
    private val delegates: MutableList<PersistenceExceptionTranslator> = ArrayList()

    /**
     * 添加一个异常翻译器到delegates当中
     *
     * @param delegate 你想要使用的PersistenceExceptionTranslator
     */
    fun addDelegate(delegate: PersistenceExceptionTranslator) = this.delegates.add(delegate)

    /**
     * 获取内部delegate的所有的[PersistenceExceptionTranslator]
     */
    fun getDelegates(): Array<PersistenceExceptionTranslator> = delegates.toTypedArray()

    /**
     * 遍历内部维护的所有的[PersistenceExceptionTranslator]尝试去进行翻译,
     * 直到最终找到一个可以去进行翻译的, 并交由它去完成真正的异常的翻译
     *
     * @param ex 待去进行翻译的异常
     * @return 翻译得到的DataAccessException, 翻译失败return null
     */
    override fun translateExceptionIfPossible(ex: RuntimeException): DataAccessException? =
        delegates.map { it.translateExceptionIfPossible(ex) }.firstOrNull { !Objects.isNull(it) }
}