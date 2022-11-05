package com.wanna.framework.test.context.support

import com.wanna.framework.test.context.TestContextBootstrapper

/**
 * 默认的[TestContextBootstrapper]的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
open class DefaultTestContextBootstrapper : AbstractTestContextBootstrapper() {
    /**
     * 对于默认的ContextLoader，我们直接使用[DelegatingSmartContextLoader]
     *
     * @param testClass testClass
     * @return DelegatingSmartContextLoader Class
     */
    override fun getDefaultContextLoaderClass(testClass: Class<*>) = DelegatingSmartContextLoader::class.java
}