package com.wanna.framework.test.context.junit4

import com.wanna.framework.test.context.*
import com.wanna.framework.test.context.junit4.statement.RunBeforeTestMethodCallbacks
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import org.slf4j.LoggerFactory

/**
 * Spring的JUnit的ClassRunner
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/4
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
     * TestContext的Manager，提供对于TestContext的管理工作，引导整个Test的启动
     */
    private val testContextManager = this.createTestContextManager(testClass)

    /**
     * 创建出来[TestContextManager]，负责去引导整个Test的启动，在各个生命周期去回调相关的监听器完成处理；
     * 根据testClass去创建出来[BootstrapContext]和[TestContextBootstrapper]，
     * [BootstrapContext]主要包含两个部分组成：testClass和[CacheAwareContextLoaderDelegate]，
     * 最终在[TestContextManager]去构建出来[TestContext]时，会将[BootstrapContext]当中的这两个部分全部转移过去
     *
     * @param testClass testClass
     * @return 创建出来的TestContextManager对象
     */
    protected open fun createTestContextManager(testClass: Class<*>): TestContextManager = TestContextManager(testClass)

    /**
     * 重写父类的创建TestInstance的逻辑，我们在这里去进行重写，提供对于TestInstance的属性填充功能
     *
     * @return 经过属性填充之后的TestInstance
     */
    override fun createTest(): Any {
        val testInstance = super.createTest()

        // 准备testInstance，对testInstance去进行属性填充
        testContextManager.prepareTestInstance(testInstance)
        return testInstance
    }

    /**
     * 重写父类的在Test方法执行之前的回调逻辑，这里给[TestContextManager]一个机会去进行更多的自定义工作
     *
     * @param testMethod testMethod
     * @param testInstance testInstance
     */
    override fun withBefores(testMethod: FrameworkMethod, testInstance: Any, statement: Statement): Statement {
        val befores = super.withBefores(testMethod, testInstance, statement)
        return RunBeforeTestMethodCallbacks(befores, testInstance, testMethod.method, testContextManager)
    }
}