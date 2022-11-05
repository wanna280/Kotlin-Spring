package com.wanna.framework.test.context.support

import com.wanna.framework.core.Ordered
import com.wanna.framework.test.context.TestContext
import com.wanna.framework.test.context.TestExecutionListener

/**
 * 抽象的[TestExecutionListener]的实现，将所有的方法去进行了实现，子类只需要重写需要的方法即可；
 * 也为[TestExecutionListener]引入了优先级的概念，借助Spring的[Ordered]去进行实现，默认为最低优先级。
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

    override fun beforeTestExecution(testContext: TestContext) {

    }

    override fun afterTestExecution(testContext: TestContext) {

    }

    override fun getOrder(): Int = Ordered.ORDER_LOWEST
}