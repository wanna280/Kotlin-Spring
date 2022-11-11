package com.wanna.framework.test.context.event

import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.test.context.TestContext
import com.wanna.framework.test.context.TestExecutionListener
import com.wanna.framework.test.context.support.AbstractTestExecutionListener

/**
 * 用于[TestContext]的相关事件的发布的[TestExecutionListener]，在合适的时机去发布对应的[TestContextEvent]事件。
 * 当监听对应事件的[ApplicationListener]将收到事件，并去对[TestContext]去进行更多自定义的操作。
 * 通过[com.wanna.framework.test.context.event.annotation]包下的相关注解，可以快速地监听到这些事件并进行自定义处理
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 *
 * @see com.wanna.framework.test.context.event.annotation.AfterTestExecution
 * @see com.wanna.framework.test.context.event.annotation.BeforeTestClass
 * @see com.wanna.framework.test.context.event.annotation.BeforeTestExecution
 * @see com.wanna.framework.test.context.event.annotation.AfterTestClass
 * @see com.wanna.framework.test.context.event.annotation.AfterTestMethod
 * @see com.wanna.framework.test.context.event.annotation.BeforeTestClass
 * @see com.wanna.framework.test.context.event.annotation.BeforeTestMethod
 * @see com.wanna.framework.test.context.event.annotation.PrepareTestInstance
 */
open class EventPublishingTestExecutionListener : AbstractTestExecutionListener() {

    /**
     * Order
     *
     * @return 10000
     */
    override fun getOrder(): Int = 10_000

    override fun beforeTestClass(testContext: TestContext) = testContext.publishEvent { BeforeTestClassEvent(it) }

    override fun afterTestClass(testContext: TestContext) = testContext.publishEvent { AfterTestClassEvent(it) }

    override fun prepareTestInstance(testContext: TestContext) =
        testContext.publishEvent { PrepareTestInstanceEvent(it) }

    override fun beforeTestMethod(testContext: TestContext) = testContext.publishEvent { BeforeTestMethodEvent(it) }

    override fun afterTestMethod(testContext: TestContext) = testContext.publishEvent { AfterTestMethodEvent(it) }

    override fun beforeTestExecution(testContext: TestContext) =
        testContext.publishEvent { BeforeTestExecutionEvent(it) }

    override fun afterTestExecution(testContext: TestContext) = testContext.publishEvent { AfterTestExecutionEvent(it) }
}