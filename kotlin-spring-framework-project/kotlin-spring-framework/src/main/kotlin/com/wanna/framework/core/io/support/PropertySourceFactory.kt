package com.wanna.framework.core.io.support

import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.core.io.Resource
import com.wanna.framework.lang.Nullable

/**
 * PropertySource的Factory，支持去去进行PropertySource的创建
 *
 * @see Resource
 * @see DefaultPropertySourceFactory
 */
interface PropertySourceFactory {

    /**
     * 创建PropertySource
     *
     * @param name name(有可能为null)
     * @param resource 资源路径
     * @return 创建好的PropertySource
     */
    fun createPropertySource(@Nullable name: String?, resource: String): PropertySource<*>

    /**
     * 根据给定的资源去创建PropertySource
     *
     * @param name name(有可能为null)
     * @param resource 资源对象
     * @return 创建好的PropertySource
     */
    fun createPropertySource(@Nullable name: String?, resource: Resource): PropertySource<*>
}