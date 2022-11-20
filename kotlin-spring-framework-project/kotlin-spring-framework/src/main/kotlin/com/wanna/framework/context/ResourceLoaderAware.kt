package com.wanna.framework.context

import com.wanna.framework.core.io.ResourceLoader

/**
 * 提供注入ResourceLoader的Aware接口
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 */
fun interface ResourceLoaderAware {

    /**
     * 自动注入ResourceLoader
     *
     * @param resourceLoader ResourceLoader
     */
    fun setResourceLoader(resourceLoader: ResourceLoader)
}