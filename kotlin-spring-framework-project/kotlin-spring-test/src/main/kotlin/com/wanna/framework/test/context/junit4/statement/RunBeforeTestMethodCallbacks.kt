package com.wanna.framework.test.context.junit4.statement

import com.wanna.framework.test.context.TestContextManager
import org.junit.Before
import org.junit.runners.model.Statement
import java.lang.reflect.Method

/**
 * 在`@Before`方法执行之前, 需要执行的Callback回调方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 *
 * @param next 责任链的下一环
 * @param testInstance testInstance
 * @param testMethod testMethod
 * @param testContextManager TestContextManager
 *
 * @see Before
 */
open class RunBeforeTestMethodCallbacks(
    private val next: Statement,
    private val testInstance: Any,
    private val testMethod: Method,
    private val testContextManager: TestContextManager
) : Statement() {

    override fun evaluate() {
        // 给TestContextManager当中的Listener一个机会去进行自定义
        testContextManager.beforeTestMethod(testInstance, testMethod)
        next.evaluate()
    }
}