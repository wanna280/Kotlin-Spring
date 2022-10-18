package com.wanna.framework.dao.support

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.BeansException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 为所有的DAO提供基础的支持的类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
abstract class DaoSupport : InitializingBean {

    /**
     * Logger
     */
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 在完成Bean的初始化时，需要去检查Dao相关的配置
     */
    override fun afterPropertiesSet() {
        checkDaoConfig()

        try {
            initDao()
        } catch (ex: Exception) {
            throw BeansException("初始化DAO失败", ex)
        }
    }

    /**
     * 检查Dao的配置，交给子类去进行实现
     */
    protected abstract fun checkDaoConfig()

    /**
     * 初始化Dao
     */
    @Throws(Exception::class)
    protected open fun initDao() {

    }
}