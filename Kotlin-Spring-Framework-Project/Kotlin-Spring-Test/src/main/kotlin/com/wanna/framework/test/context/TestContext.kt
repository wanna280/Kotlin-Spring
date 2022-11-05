package com.wanna.framework.test.context

import com.wanna.framework.beans.factory.support.definition.config.AttributeAccessor
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.lang.Nullable
import java.io.Serializable
import java.lang.reflect.Method

/**
 * 维护了测试相关的上下文信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/4
 */
interface TestContext : AttributeAccessor, Serializable {

    /**
     * 从[TestContext]当中获取到[ApplicationContext]
     *
     * @return ApplicationContext
     */
    fun getApplicationContext(): ApplicationContext

    /**
     * 获取testClass
     *
     * @return testClass
     */
    fun getTestClass(): Class<*>

    /**
     * 获取testInstance
     *
     * @return testInstance(获取不到return null)
     */
    @Nullable
    fun getTestInstance(): Any?

    /**
     * 获取testMethod
     *
     * @return testMethod(获取不到return null)
     */
    @Nullable
    fun getTestMethod(): Method?

    /**
     * 获取testException
     *
     * @return testException(获取不到return null)
     */
    @Nullable
    fun getTestException(): Throwable?

    /**
     * 更新当前[TestContext]的状态信息(testInstance/testMethod/testException)
     *
     * @param testInstance Test实例对象(可能为null)
     * @param testMethod Test方法(可能为null)
     * @param testException Test过程出现的异常(可能为null)
     */
    fun updateState(@Nullable testInstance: Any?, @Nullable testMethod: Method?, @Nullable testException: Throwable?)
}