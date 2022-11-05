package com.wanna.framework.test.context.support

import com.wanna.framework.test.context.BootstrapContext
import com.wanna.framework.test.context.CacheAwareContextLoaderDelegate

/**
 * 默认的[BootstrapContext]的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 *
 * @param testClass testClass
 * @param cacheAwareContextLoaderDelegate CacheAwareContextLoaderDelegate
 */
open class DefaultBootstrapContext(
    private val testClass: Class<*>,
    private val cacheAwareContextLoaderDelegate: CacheAwareContextLoaderDelegate
) : BootstrapContext {
    override fun getTestClass() = this.testClass
    override fun getCacheAwareContextLoaderDelegate() = this.cacheAwareContextLoaderDelegate
}