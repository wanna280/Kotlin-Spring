package com.wanna.framework.test.context

import com.wanna.framework.lang.Nullable
import com.wanna.framework.test.context.BootstrapUtils.createBootstrapContext
import com.wanna.framework.test.context.BootstrapUtils.resolveTestContextBootstrapper
import java.lang.reflect.Method

/**
 * [TestContext]的管理器，提供对于[TestContext]的管理工作，对于JUnit4和JUnit5都基于TestContextManager去去进行核心的实现。
 * 在JUnit的对应生命周期方法执行时，将事件告知所有的[TestExecutionListener]，让监听器去处理该事件，实现用户的自定义。
 * 本质上当前的[TestContextManager]就相当于一个事件派发器，参考[com.wanna.framework.context.event.ApplicationEventMulticaster]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/4
 *
 * @param testContextBootstrapper TestContextBootstrapper，主要作用在于去构建出来[TestContext]和[TestExecutionListener]
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
     * [TestExecutionListener]列表，监听JUnit的整个执行流程[TestContext]，
     * 当前的[TestContextManager]的主要作用就是在合适的时机去触发这些监听器的对应方法。
     */
    private val testExecutionListeners = ArrayList<TestExecutionListener>()

    init {
        // 注册所有的TestContextBootstrapper当中的所有的TestExecutionListeners
        this.registerTestExecutionListeners(testContextBootstrapper.getTestExecutionListeners())
    }

    /**
     * 注册[TestExecutionListener]到当前[TestContextManager]当中
     *
     * @param testExecutionListeners 想要去进行注册的[TestExecutionListener]列表
     */
    open fun registerTestExecutionListeners(testExecutionListeners: List<TestExecutionListener>) {
        testExecutionListeners.forEach(this.testExecutionListeners::add)
    }

    /**
     * 注册[TestExecutionListener]到当前[TestContextManager]当中
     *
     * @param testExecutionListeners 想要去进行注册的[TestExecutionListener]列表
     */
    open fun registerTestExecutionListeners(vararg testExecutionListeners: TestExecutionListener) {
        testExecutionListeners.forEach(this.testExecutionListeners::add)
    }

    /**
     * 在`@BeforeTestClass`(JUnit5的`@BeforeAll`)方法执行之前，回调所有的监听器，给它们一个机会去对[TestContext]去进行自定义
     */
    open fun beforeTestClass() {
        testExecutionListeners.forEach {
            it.beforeTestClass(testContext)
        }
    }

    /**
     * 在`@BeforeTestClass`(JUnit5的`@AfterAll`)方法执行之后，回调所有的监听器，给它们一个机会去对[TestContext]去进行自定义
     */
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
     * 在`@Before`(JUnit5的`@BeforeEach`)方法执行之前，回调所有的监听器，给它们一个机会去对[TestContext]去进行自定义
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
     * 在`@After`(JUnit5的`@AfterEach`)方法执行之后，回调所有的监听器，给它们一个机会去对[TestContext]去进行自定义
     *
     * @param testInstance TestInstance
     * @param testMethod Test方法
     */
    open fun afterTestMethod(testInstance: Any, testMethod: Method, @Nullable testException: Throwable?) {
        // 更新TestContext的状态
        testContext.updateState(testInstance, testMethod, testException)
        testExecutionListeners.forEach {
            try {
                it.afterTestMethod(testContext)
            } catch (ex: Exception) {
                throw ex
            }
        }
    }

    /**
     * 在`@Test`方法执行之前，回调所有的监听器，给它们一个机会去对[TestContext]进行自定义
     *
     * @param testInstance testInstance
     * @param testMethod testMethod
     */
    open fun beforeTestExecution(testMethod: Method, testInstance: Any) {
        // 更新TestContext的状态
        testContext.updateState(testInstance, testMethod, null)
        testExecutionListeners.forEach {
            try {
                it.beforeTestExecution(testContext)
            } catch (ex: Exception) {
                throw ex
            }
        }
    }

    /**
     * 在`@Test`方法执行之后，回调所有的监听器，给它们一个机会去对[TestContext]进行自定义
     *
     * @param testInstance testInstance
     * @param testMethod testMethod
     * @param testException 执行Test方法之后，出现的异常
     */
    open fun afterTestExecution(testMethod: Method, testInstance: Any, @Nullable testException: Throwable?) {
        // 更新TestContext的状态
        testContext.updateState(testInstance, testMethod, testException)
        testExecutionListeners.forEach {
            it.afterTestExecution(testContext)
        }
    }
}