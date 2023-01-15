package com.wanna.framework.test.context.junit4.statement

import com.wanna.framework.test.context.TestContextManager
import org.junit.runners.model.Statement
import java.lang.reflect.Method

/**
 * 在一个`@Test`方法执行之前, 需要执行的Callback
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
open class RunBeforeTestExecutionCallbacks(
    private val next: Statement,
    private val testMethod: Method,
    private val testInstance: Any,
    private val testContextManager: TestContextManager
) : Statement() {

    override fun evaluate() {
        testContextManager.beforeTestExecution(testMethod, testInstance)
        next.evaluate()
    }
}