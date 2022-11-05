package com.wanna.framework.test.context.junit.jupiter

import com.wanna.framework.test.context.TestContextManager
import com.wanna.framework.test.context.junit4.SpringJUnit4ClassRunner
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.*
import org.junit.runner.RunWith

/**
 * 针对JUnit5实现的Extension，对于JUnit5的各个生命周期去进行自定义，对应JUnit4的[SpringJUnit4ClassRunner]。
 *
 * 在JUnit4当中，采用的方式通过重写模板方法去实现的添加Callback方法；在JUnit5当中，则为每个Callback单独抽离成为了一个
 * 接口去作为Callback，无需通过重写模板方法的方式去进行实现。
 *
 * 在JUnit4当中，对于Runner需要配合[RunWith]注解去进行使用；在JUnit5当中，SpringExtension则需要配合[ExtendWith]注解去进行使用。
 *
 * 使用参考示例代码：
 * ```kotlin
 * @ExtendWith(SpringExtension::class.java)
 * @ContextConfiguration(locations = "classpath:context.xml")
 * class AppTest {
 *     @Test
 *     fun test() {
 *       // do something...
 *     }
 * }
 * ```
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 *
 * @see ExtendWith
 */
open class SpringExtension : BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor, BeforeEachCallback,
    AfterEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver {

    companion object {
        /**
         * TestContextManager的Namespace
         */
        @JvmStatic
        private val TEST_CONTEXT_MANAGER_NAMESPACE =
            ExtensionContext.Namespace.create(arrayOf(SpringExtension::class.java))

        /**
         * 根据[ExtensionContext]去获取到[TestContextManager]
         *
         * @param context JUnit ExtensionContext
         * @return TestContextManager
         */
        @JvmStatic
        fun getTestContextManager(context: ExtensionContext): TestContextManager {
            val testClass = context.requiredTestClass
            val store = context.root.getStore(TEST_CONTEXT_MANAGER_NAMESPACE)
            return store.getOrComputeIfAbsent(
                testClass,
                { TestContextManager(it) },
                TestContextManager::class.java
            )
        }
    }

    /**
     * 在[BeforeAll]注解的方法(对应JUnit4当中的`@BeforeClass`的方法)执行之前，需要触发的Callback
     *
     * @param context JUnit的ExtensionContext(维护了testInstance、testClass、testMethod的上下文信息)
     */
    override fun beforeAll(context: ExtensionContext) {
        getTestContextManager(context).beforeTestClass()
    }

    /**
     * 在[AfterAll]注解的方法(对应JUnit4当中的`@AfterClass`的方法)执行之后，需要触发的Callback
     *
     * @param context JUnit的ExtensionContext(维护了testInstance、testClass、testMethod的上下文信息)
     */
    override fun afterAll(context: ExtensionContext) {
        getTestContextManager(context).afterTestClass()
    }

    /**
     * 在[BeforeEach]注解的方法(对应JUnit4当中的`@Before`的方法)执行之前，需要触发的Callback
     *
     * @param context JUnit的ExtensionContext(维护了testInstance、testClass、testMethod的上下文信息)
     */
    override fun beforeEach(context: ExtensionContext) {
        getTestContextManager(context).beforeTestMethod(context.testInstance, context.requiredTestMethod)
    }

    /**
     * 在[AfterEach]注解的方法(对应JUnit4当中的`@After`的方法)执行之后，需要触发的Callback
     *
     * @param context JUnit的ExtensionContext(维护了testInstance、testClass、testMethod的上下文信息)
     */
    override fun afterEach(context: ExtensionContext) {
        getTestContextManager(context).afterTestMethod(context.testInstance, context.requiredTestMethod)
    }

    /**
     * 对创建出来的testInstance去进行后置处理
     *
     * @param testInstance testInstance
     * @param context JUnit的ExtensionContext(维护了testInstance、testClass、testMethod的上下文信息)
     */
    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        getTestContextManager(context).prepareTestInstance(testInstance)
    }


    /**
     * 在[Test]注解的方法执行之前需要执行的Callback
     *
     * @param context JUnit的ExtensionContext(维护了testInstance、testClass、testMethod的上下文信息)
     */
    @Test
    override fun beforeTestExecution(context: ExtensionContext) {
        getTestContextManager(context).beforeTestExecution(context.requiredTestMethod, context.testInstance)
    }

    /**
     * 在[Test]注解的方法执行之后需要执行的Callback
     *
     * @param context JUnit的ExtensionContext(维护了testInstance、testClass、testMethod的上下文信息)
     */
    override fun afterTestExecution(context: ExtensionContext) {
        getTestContextManager(context).afterTestExecution(context.requiredTestMethod, context.testInstance)
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return false
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return Any()
    }
}