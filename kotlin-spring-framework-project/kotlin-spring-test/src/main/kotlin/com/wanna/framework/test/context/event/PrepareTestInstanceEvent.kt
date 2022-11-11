package com.wanna.framework.test.context.event

import com.wanna.framework.test.context.TestContext

/**
 * 当TestInstance实例对象进行准备的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
open class PrepareTestInstanceEvent(testContext: TestContext) : TestContextEvent(testContext)