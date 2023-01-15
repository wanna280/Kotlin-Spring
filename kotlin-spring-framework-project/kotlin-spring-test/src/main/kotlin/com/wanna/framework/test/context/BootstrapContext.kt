package com.wanna.framework.test.context

/**
 * Test应用的启动上下文, 维护testClass和CacheAwareContextLoaderDelegate
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
interface BootstrapContext {
    /**
     * 获取testClass
     *
     * @return testClass
     */
    fun getTestClass(): Class<*>

    /**
     * 获取CacheAwareContextLoaderDelegate
     *
     * @return CacheAwareContextLoaderDelegate
     */
    fun getCacheAwareContextLoaderDelegate(): CacheAwareContextLoaderDelegate
}