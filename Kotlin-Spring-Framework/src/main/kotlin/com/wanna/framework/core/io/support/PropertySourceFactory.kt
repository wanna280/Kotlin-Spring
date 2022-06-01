package com.wanna.framework.core.io.support

import com.wanna.framework.core.environment.PropertySource

/**
 * PropertySource的Factory，支持去去进行PropertySource的创建
 */
interface PropertySourceFactory {

    /**
     * 创建PropertySource
     *
     * @param name name(有可能为null)
     * @param resource 资源路径
     * @return 创建好的PropertySource
     */
    fun createPropertySource(name: String?, resource: String): PropertySource<*>
}