package com.wanna.framework.test.context.event

import com.wanna.framework.test.context.TestContext

/**
 * 在`@AfterClass`(JUnit5的`@AfterAll`)的方法执行之后发布的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 *
 * @param testContext TestContext
 */
open class AfterTestClassEvent(testContext: TestContext) : TestContextEvent(testContext)