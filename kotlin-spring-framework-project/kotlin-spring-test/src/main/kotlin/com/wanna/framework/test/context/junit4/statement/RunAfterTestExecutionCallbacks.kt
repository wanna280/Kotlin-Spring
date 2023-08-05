package com.wanna.framework.test.context.junit4.statement

import com.wanna.framework.test.context.TestContextManager
import org.junit.runners.model.Statement
import java.lang.reflect.Method

/**
 * 在一个`@Test`方法执行完成之后的, 需要去执行的Callback
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

        // 记录执行过程当中的异常
        var testException: Throwable? = null
        try {
            next.evaluate()
        } catch (ex: Throwable) {
            testException = ex
        }

        testContextManager.afterTestExecution(testMethod, testInstance, testException)
    }
}