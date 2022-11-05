package com.wanna.framework.test.context

import com.wanna.framework.context.ApplicationContext

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
interface SmartContextLoader : ContextLoader {

    /**
     * 对于[ContextConfigurationAttributes]去进行自定义处理
     *
     * @param configAttributes @ContextConfiguration注解当中的属性信息
     */
    fun processContextConfiguration(configAttributes: ContextConfigurationAttributes)

    /**
     * 根据[MergedContextConfiguration]去加载得到[ApplicationContext]
     *
     * @param mergedContextConfiguration 多个[ContextConfigurationAttributes]去进行Merge的结果
     * @return ApplicationContext
     */
    fun loadContext(mergedContextConfiguration: MergedContextConfiguration): ApplicationContext
}