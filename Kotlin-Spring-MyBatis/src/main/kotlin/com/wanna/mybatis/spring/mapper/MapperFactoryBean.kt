package com.wanna.mybatis.spring.mapper

import com.wanna.framework.beans.factory.FactoryBean
import com.wanna.mybatis.spring.support.SqlSessionDaoSupport

/**
 * Mapper的FactoryBean，负责给SpringBeanFactory当中去导入Mapper，使用SqlSession去getMapper
 *
 * @param mapperInterface 包装的Mapper接口
 *
 * @see SqlSessionDaoSupport
 */
open class MapperFactoryBean(var mapperInterface: Class<*>? = null) : FactoryBean<Any>, SqlSessionDaoSupport() {

    // 是否需要添加到Configuration当中？
    var addToConfig = true

    /**
     * 检查Dao的配置，会在初始化Bean时，会自动回调这个方法
     */
    override fun checkDaoConfig() {
        checkNotNull(mapperInterface) { "MapperFactoryBean的mapperInterface不能为空" }
        val configuration = getSqlSession().configuration
        // 如果需要添加到配置当中，并且没有注册过该Mapper的话，那么去进行注册
        if (this.addToConfig && !configuration.hasMapper(this.mapperInterface)) {
            configuration.addMapper(this.mapperInterface)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getObjectType(): Class<Any> = (mapperInterface as Class<Any>)
    override fun getObject(): Any = getSqlSession().getMapper(mapperInterface!!)
    override fun isSingleton() = true
    override fun isPrototype() = false
}