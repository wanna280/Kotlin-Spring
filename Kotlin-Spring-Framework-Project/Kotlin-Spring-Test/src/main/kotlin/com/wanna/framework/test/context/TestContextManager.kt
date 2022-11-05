package com.wanna.framework.test.context

import com.wanna.framework.test.context.BootstrapUtils.createBootstrapContext
import com.wanna.framework.test.context.BootstrapUtils.resolveTestContextBootstrapper
import java.lang.reflect.Method

/**
 * [TestContext]的管理器，提供对于[TestContext]的管理工作，对于JUnit4和JUnit5都基于TestContextManager去去进行核心的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/4
 *
 * @param testContextBootstrapper TestContextBootstrapper
 */
open class TestContextManager(testContextBootstrapper: TestContextBootstrapper) {

    /**
     * 支持直接去传入一个testClass，从testClass当中去进行各种注解的解析从而去自动构建出来合适的[TestContextBootstrapper]
     *
     * @param testClass testClass
     */
    constructor(testClass: Class<*>) : this(resolveTestContextBootstrapper(createBootstrapContext(testClass)))

    /**
     * 使用[TestContextBootstrapper]构建出来[TestContext]
     */
    private val testContext = testContextBootstrapper.buildTestContext()

    /**
     * [TestExecutionListener]列表，监听[TestContext]的整个执行流程
     */
    private val testExecutionListeners = ArrayList<TestExecutionListener>()

    init {
        // 注册所有的TestExecutionListeners
        this.registerTestExecutionListeners(testContextBootstrapper.getTestExecutionListeners())
    }

    open fun registerTestExecutionListeners(testExecutionListeners: List<TestExecutionListener>) {
        testExecutionListeners.forEach(this.testExecutionListeners::add)
    }

    open fun registerTestExecutionListeners(vararg testExecutionListeners: TestExecutionListener) {
        testExecutionListeners.forEach(this.testExecutionListeners::add)
    }

    open fun beforeTestClass() {
        testExecutionListeners.forEach {
            it.beforeTestClass(testContext)
        }
    }

    open fun afterTestClass() {
        testExecutionListeners.forEach {
            it.afterTestClass(testContext)
        }
    }


    /**
     * 准备Test的实例对象
     *
     * @param testInstance Test的实例对象
     */
    open fun prepareTestInstance(testInstance: Any) {
        // 更新TestContext的状态
        testContext.updateState(testInstance, null, null)

        testExecutionListeners.forEach {
            try {
                it.prepareTestInstance(testContext)
            } catch (ex: Exception) {
                throw ex
            }
        }
    }

    /**
     * 在执行Test方法之前的后置处理工作
     *
     * @param testInstance TestInstance
     * @param testMethod Test方法
     */
    open fun beforeTestMethod(testInstance: Any, testMethod: Method) {
        // 更新TestContext的状态
        testContext.updateState(testInstance, testMethod, null)
        testExecutionListeners.forEach {
            try {
                it.beforeTestMethod(testContext)
            } catch (ex: Exception) {
                throw ex
            }
        }
    }

    /**
     * 在执行Test方法之后的后置处理工作
     *
     * @param testInstance TestInstance
     * @param testMethod Test方法
     */
    open fun afterTestMethod(testInstance: Any, testMethod: Method) {
        // 更新TestContext的状态
        testContext.updateState(testInstance, testMethod, null)
        testExecutionListeners.forEach {
            try {
                it.afterTestMethod(testContext)
            } catch (ex: Exception) {
                throw ex
            }
        }
    }

    open fun beforeTestExecution(testMethod: Method, testInstance: Any) {
        testExecutionListeners.forEach {
            try {
                it.beforeTestExecution(testContext)
            } catch (ex: Exception) {
                throw ex
            }
        }
    }

    open fun afterTestExecution(testMethod: Method, testInstance: Any) {
        testExecutionListeners.forEach {
            it.afterTestExecution(testContext)
        }
    }
}