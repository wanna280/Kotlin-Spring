package com.wanna.framework.test.context

/**
 * Test任务执行的监听器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/4
 */
interface TestExecutionListener {

    fun beforeTestClass(testContext: TestContext)

    fun prepareTestInstance(testContext: TestContext)

    fun beforeTestMethod(testContext: TestContext)

    fun afterTestMethod(testContext: TestContext)

    fun afterTestClass(testContext: TestContext)
}