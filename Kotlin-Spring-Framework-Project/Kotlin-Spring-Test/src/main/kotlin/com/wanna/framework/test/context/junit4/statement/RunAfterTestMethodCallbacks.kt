package com.wanna.framework.test.context.junit4.statement

import com.wanna.framework.test.context.TestContextManager
import org.junit.After
import org.junit.runners.model.Statement
import java.lang.reflect.Method

/**
 * 在`@After`方法执行之后，需要执行的Callback回调方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 *
 * @see After
 */
open class RunAfterTestMethodCallbacks(
    private val next: Statement,
    private val testInstance: Any,
    private val testMethod: Method,
    private val testContextManager: TestContextManager
) : Statement() {

    override fun evaluate() {
        next.evaluate()
        testContextManager.afterTestMethod(testInstance, testMethod)
    }
}