package com.wanna.framework.test.context

import com.wanna.framework.context.ApplicationContext

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
interface CacheAwareContextLoaderDelegate {

    /**
     * 根据[MergedContextConfiguration]配置信息去加载得到[ApplicationContext]
     *
     * @param mergedContextConfiguration Merged ContextConfiguration
     * @return 加载得到的[ApplicationContext]
     */
    fun loadApplicationContext(mergedContextConfiguration: MergedContextConfiguration): ApplicationContext

    /**
     * 关闭[ApplicationContext]
     *
     * @param mergedContextConfiguration Merged ContextConfiguration
     */
    fun closeContext(mergedContextConfiguration: MergedContextConfiguration)
}