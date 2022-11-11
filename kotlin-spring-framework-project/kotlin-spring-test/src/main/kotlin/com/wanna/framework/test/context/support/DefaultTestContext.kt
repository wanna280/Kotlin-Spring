package com.wanna.framework.test.context.support

import com.wanna.framework.beans.factory.support.definition.config.AttributeAccessorSupport
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.lang.Nullable
import com.wanna.framework.test.context.CacheAwareContextLoaderDelegate
import com.wanna.framework.test.context.ContextLoader
import com.wanna.framework.test.context.MergedContextConfiguration
import com.wanna.framework.test.context.TestContext
import java.lang.reflect.Method

/**
 * 对于[TestContext]的默认实现，维护了当前Test应用的上下文信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/4
 *
 * @param testClass testClass测试类
 * @param mergedContextConfiguration MergedContextConfiguration
 * @param cacheAwareContextLoaderDelegate 根据[MergedContextConfiguration]去获取到[ApplicationContext]的LoaderDelegate
 */
open class DefaultTestContext(
    private val testClass: Class<*>,
    private val mergedContextConfiguration: MergedContextConfiguration,
    private val cacheAwareContextLoaderDelegate: CacheAwareContextLoaderDelegate
) : TestContext, AttributeAccessorSupport() {

    /**
     * testInstance
     */
    @Nullable
    private var testInstance: Any? = null

    /**
     * testMethod
     */
    @Nullable
    private var testMethod: Method? = null

    /**
     * testException
     */
    @Nullable
    private var textException: Throwable? = null

    /**
     * 获取到[ApplicationContext]，直接使用[CacheAwareContextLoaderDelegate]，
     * 根据[MergedContextConfiguration]，使用[ContextLoader]去进行构建出来一个合适的[ApplicationContext]
     *
     * @return ApplicationContext
     */
    override fun getApplicationContext(): ApplicationContext {
        return cacheAwareContextLoaderDelegate.loadApplicationContext(mergedContextConfiguration)
    }

    /**
     * 获取到testClass
     *
     * @return testClass
     */
    override fun getTestClass(): Class<*> = this.testClass

    /**
     * 获取到testInstance
     *
     * @return testInstance(如果没有初始化，那么return null)
     */
    @Nullable
    override fun getTestInstance(): Any? = this.testInstance

    /**
     * 获取到testMethod
     *
     * @return testMethod(如果没有初始化，那么return null)
     */
    @Nullable
    override fun getTestMethod(): Method? = this.testMethod

    /**
     * 获取到testException
     *
     * @return testException(如果没有初始化，那么return null)
     */
    @Nullable
    override fun getTestException(): Throwable? = this.textException

    /**
     * 更新当前的testInstance、testMethod、testException的状态
     *
     * @param testInstance testInstance
     * @param testMethod testMethod
     * @param testException testException
     */
    override fun updateState(
        @Nullable testInstance: Any?,
        @Nullable testMethod: Method?,
        @Nullable testException: Throwable?
    ) {
        this.testInstance = testInstance
        this.testMethod = testMethod
        this.textException = testException
    }
}