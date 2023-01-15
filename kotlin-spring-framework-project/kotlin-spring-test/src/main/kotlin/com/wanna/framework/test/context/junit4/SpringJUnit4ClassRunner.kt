package com.wanna.framework.test.context.junit4

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.test.context.*
import com.wanna.framework.test.context.junit.jupiter.SpringExtension
import com.wanna.framework.test.context.junit4.statement.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.internal.runners.model.ReflectiveCallable
import org.junit.internal.runners.statements.Fail
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import org.slf4j.LoggerFactory

/**
 * Spring的JUnit4的ClassRunner, 负责去自定义JUnit执行的各个生命周期;
 * 对应JUnit5的[SpringExtension], 在JUnit4当中需要在测试类当中去配合JUnit4的[RunWith]注解使用.
 * 在JUnit实现的[BlockJUnit4ClassRunner]当中已经为我们定义好了一个测试类需要去进行执行的生命周期回调方法,
 * 我们只需要重写JUnit当中对应的生命周期回调方法, 去实现Spring的更多自定义即可
 *
 * 使用参考示例代码：
 *
 * ```kotlin
 * @RunWith(SpringJUnit4ClassRunner::class)
 * @ContextConfiguration(locations = "classpath:context.xml")
 * class AppTest {
 *     @Test
 *     fun test() {
 *       // do something...
 *     }
 * }
 * ```
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/4
 *
 * @see RunWith
 * @see BlockJUnit4ClassRunner
 * @see org.junit.runner.Runner
 *
 * @param testClass testClass
 */
open class SpringJUnit4ClassRunner(testClass: Class<*>) : BlockJUnit4ClassRunner(testClass) {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(SpringJUnit4ClassRunner::class.java)
    }

    /**
     * TestContext的Manager, 提供对于TestContext的管理工作, 引导整个Test的启动
     */
    private val testContextManager = this.createTestContextManager(testClass)

    /**
     * 创建出来[TestContextManager], 负责去引导整个Test的启动, 在各个生命周期去回调相关的监听器完成处理;
     * 根据testClass去创建出来[BootstrapContext]和[TestContextBootstrapper],
     * [BootstrapContext]主要包含两个部分组成：testClass和[CacheAwareContextLoaderDelegate],
     * 最终在[TestContextManager]去构建出来[TestContext]时, 会将[BootstrapContext]当中的这两个部分全部转移过去
     *
     * @param testClass testClass
     * @return 创建出来的TestContextManager对象
     */
    protected open fun createTestContextManager(testClass: Class<*>): TestContextManager = TestContextManager(testClass)

    /**
     * 获取[TestContextManager]
     *
     * @return TestContextManager
     */
    protected fun getTestContextManager(): TestContextManager = this.testContextManager

    /**
     * 构建方法块执行的拦截器链
     *
     * @param frameworkMethod testMethod
     * @return 执行方法时用到的Statement责任链(整体设计和Servlet的Filter类似, 使用evaluate方法去放行到下一环)
     */
    override fun methodBlock(frameworkMethod: FrameworkMethod): Statement {
        val testInstance: Any = try {
            object : ReflectiveCallable() {
                override fun runReflectiveCall(): Any = createTest()
            }.run()
        } catch (e: Throwable) {
            return Fail(e)
        }

        // 创建一个@Test方法的MethodInvoker的Statement
        var statement = methodInvoker(frameworkMethod, testInstance)

        // 在@Test方法之前去添加一个Callback
        statement = withBeforeTestExecutionCallbacks(frameworkMethod, testInstance, statement)

        // 在@Test方法之后添加一个Callback
        statement = withAfterTestExecutionCallbacks(frameworkMethod, testInstance, statement)

        // 如果必要的话, 添加一个处理预期异常的Callback
        statement = possiblyExpectingExceptions(frameworkMethod, testInstance, statement)

        // 添加@Before的Callback
        statement = withBefores(frameworkMethod, testInstance, statement)

        // 添加@After的Callback
        statement = withAfters(frameworkMethod, testInstance, statement)

        // 如果必要的话, 添加处理@Test上的timeout属性的Callback
        statement = withPotentialTimeout(frameworkMethod, testInstance, statement)
        return statement
    }

    /**
     * 重写父类的创建TestInstance的逻辑, 我们在这里去进行重写, 对于TestInstance去进行功能的扩展
     * 提供基于Spring的[ApplicationContext]去对于TestInstance的属性填充功能
     *
     * @return 经过[ApplicationContext]进行属性填充之后的TestInstance
     */
    override fun createTest(): Any {
        val testInstance = super.createTest()
        // 准备testInstance, 对testInstance去进行属性填充
        getTestContextManager().prepareTestInstance(testInstance)
        return testInstance
    }

    /**
     * 在Test方法执行之前, 需要触发的Callback
     *
     * @param frameworkMethod testMethod
     * @param testInstance testInstance
     * @param statement JUnit的Statement责任链的当前环节
     */
    protected open fun withBeforeTestExecutionCallbacks(
        frameworkMethod: FrameworkMethod,
        testInstance: Any,
        statement: Statement
    ): Statement {
        return RunBeforeTestExecutionCallbacks(statement, frameworkMethod.method, testInstance, getTestContextManager())
    }

    /**
     * 在Test方法执行之后, 需要触发的Callback
     *
     * @param frameworkMethod testMethod
     * @param testInstance testInstance
     * @param statement JUnit的Statement责任链的当前环节
     */
    protected open fun withAfterTestExecutionCallbacks(
        frameworkMethod: FrameworkMethod,
        testInstance: Any,
        statement: Statement
    ): Statement {
        return RunAfterTestExecutionCallbacks(statement, frameworkMethod.method, testInstance, getTestContextManager())
    }

    /**
     * 在`@Before`注解标注的方法执行之前需要执行的回调Callback逻辑, 这里给[TestContextManager]
     * 一个在`@Before`注解标注的方法执行之前, 去进行更多的自定义工作
     *
     * @param testMethod testMethod
     * @param testInstance testInstance
     * @param statement JUnit的Statement责任链的当前环节
     * @see Before
     */
    override fun withBefores(testMethod: FrameworkMethod, testInstance: Any, statement: Statement): Statement {
        val withBefores = super.withBefores(testMethod, testInstance, statement)
        return RunBeforeTestMethodCallbacks(withBefores, testInstance, testMethod.method, getTestContextManager())
    }

    /**
     * 在`@After`注解标注的方法执行之后需要执行的回调Callback逻辑, 这里给[TestContextManager]
     * 一个在`@After`注解标注的方法执行之后, 去进行更多的自定义工作
     *
     * @param testMethod testMethod
     * @param testInstance testInstance
     * @param statement JUnit的Statement责任链的当前环节
     * @see After
     */
    override fun withAfters(testMethod: FrameworkMethod, testInstance: Any, statement: Statement): Statement {
        val withAfters = super.withAfters(testMethod, testInstance, statement)
        return RunAfterTestMethodCallbacks(withAfters, testInstance, testMethod.method, getTestContextManager())
    }

    /**
     * 在`@BeforeTestClass`注解标注的方法执行之前需要执行的回调Callback逻辑, 这里给[TestContextManager]
     * 一个在`@BeforeTestClass`注解标注的方法执行之前, 去进行更多的自定义工作
     *
     * @param statement JUnit的Statement责任链的当前环节
     * @see BeforeClass
     */
    override fun withBeforeClasses(statement: Statement): Statement {
        val withBeforeClasses = super.withBeforeClasses(statement)
        return RunBeforeTestClassCallbacks(withBeforeClasses, getTestContextManager())
    }

    /**
     * 在`@AfterClass`注解标注的方法执行之后需要执行的回调Callback逻辑, 这里给[TestContextManager]
     * 一个在`@AfterClass`注解标注的方法执行之后, 去进行更多的自定义工作
     *
     * @param statement JUnit的Statement责任链的当前环节
     * @see AfterClass
     */
    override fun withAfterClasses(statement: Statement): Statement {
        val withAfterClasses = super.withAfterClasses(statement)
        return RunAfterTestClassCallbacks(withAfterClasses, getTestContextManager())
    }
}