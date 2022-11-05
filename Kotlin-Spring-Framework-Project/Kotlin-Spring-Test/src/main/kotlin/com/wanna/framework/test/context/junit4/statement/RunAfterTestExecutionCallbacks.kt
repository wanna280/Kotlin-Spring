package com.wanna.framework.test.context.junit4.statement

import com.wanna.framework.test.context.TestContextManager
import org.junit.runners.model.Statement
import java.lang.reflect.Method

/**
 * 在一个`@Test`方法执行完成之后的，需要去执行的Callback
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
open class RunAfterTestExecutionCallbacks(
    private val next: Statement,
    private val testMethod: Method,
    private val testInstance: Any,
    private val testContextManager: TestContextManager
) : Statement() {

    override fun evaluate() {
        next.evaluate()
        testContextManager.afterTestExecution(testMethod, testInstance)
    }
}