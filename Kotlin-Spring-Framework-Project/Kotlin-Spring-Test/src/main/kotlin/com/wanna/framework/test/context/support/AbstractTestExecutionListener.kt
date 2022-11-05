package com.wanna.framework.test.context.support

import com.wanna.framework.core.Ordered
import com.wanna.framework.test.context.TestContext
import com.wanna.framework.test.context.TestExecutionListener

/**
 * 抽象的[TestExecutionListener]的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
abstract class AbstractTestExecutionListener : TestExecutionListener, Ordered {

    override fun beforeTestClass(testContext: TestContext) {

    }

    override fun prepareTestInstance(testContext: TestContext) {

    }

    override fun beforeTestMethod(testContext: TestContext) {

    }

    override fun afterTestMethod(testContext: TestContext) {

    }

    override fun afterTestClass(testContext: TestContext) {

    }

    override fun getOrder(): Int = Ordered.ORDER_LOWEST
}