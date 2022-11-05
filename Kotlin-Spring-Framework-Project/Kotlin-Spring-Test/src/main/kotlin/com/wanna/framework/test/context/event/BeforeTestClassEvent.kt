package com.wanna.framework.test.context.event

import com.wanna.framework.test.context.TestContext

/**
 * 在`@BeforeClass`(JUnit5的`@BeforeAll`)的方法执行之前发布的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 *
 * @param testContext TestContext
 */
open class BeforeTestClassEvent(testContext: TestContext) : TestContextEvent(testContext)