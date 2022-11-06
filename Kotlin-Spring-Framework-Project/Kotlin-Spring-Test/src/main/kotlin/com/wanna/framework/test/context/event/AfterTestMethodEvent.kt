package com.wanna.framework.test.context.event

import com.wanna.framework.test.context.TestContext

/**
 * 在`@After`(JUnit5的`@AfterEach`)的方法执行之后触发的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 *
 * @param testContext TestContext
 */
open class AfterTestMethodEvent(testContext: TestContext) : TestContextEvent(testContext)