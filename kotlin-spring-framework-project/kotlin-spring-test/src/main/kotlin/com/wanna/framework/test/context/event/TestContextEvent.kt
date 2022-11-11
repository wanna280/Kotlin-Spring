package com.wanna.framework.test.context.event

import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.test.context.TestContext

/**
 * 事件来源是[TestContext]的[ApplicationEvent]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 *
 * @param testContext testContext
 */
abstract class TestContextEvent(private val testContext: TestContext) : ApplicationEvent(testContext) {
    override fun getSource(): TestContext = testContext
}