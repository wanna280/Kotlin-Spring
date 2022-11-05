package com.wanna.framework.test.context.event

import com.wanna.framework.test.context.TestContext
import com.wanna.framework.test.context.TestExecutionListener
import com.wanna.framework.test.context.support.AbstractTestExecutionListener

/**
 * 用于[TestContext]的相关事件的发布的[TestExecutionListener]，在合适的时机去发布对应的[TestContextEvent]事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
open class EventPublishingTestExecutionListener : AbstractTestExecutionListener() {

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