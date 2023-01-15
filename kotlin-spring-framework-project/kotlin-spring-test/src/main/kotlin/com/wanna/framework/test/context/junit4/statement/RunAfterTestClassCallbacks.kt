package com.wanna.framework.test.context.junit4.statement

import com.wanna.framework.test.context.TestContextManager
import org.junit.AfterClass
import org.junit.runners.model.Statement

/**
 * 在`@AfterClass`注解标注的方法执行之后, 需要执行的Callback
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 *
 * @param next 下一步要去执行的Statement
 * @param testContextManager TestContextManager
 *
 * @see AfterClass
 */
open class RunAfterTestClassCallbacks(
    private val next: Statement,
    private val testContextManager: TestContextManager
) : Statement() {

    override fun evaluate() {
        var testException: Throwable? = null
        try {
            next.evaluate()
        } catch (ex: Throwable) {
            testException = ex
        }
        this.testContextManager.afterTestClass()
    }
}