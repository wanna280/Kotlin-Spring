package com.wanna.framework.test.context.support

import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory.Companion.AUTOWIRE_NO
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory.Companion.ORIGINAL_INSTANCE_SUFFIX
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.test.context.TestContext
import com.wanna.framework.test.context.TestExecutionListener

/**
 * 为TestInstance去提供依赖注入的[TestExecutionListener], 借助[ApplicationContext]去为TestInstance去进行属性填充
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 *
 * @see injectDependencies
 * @see AutowireCapableBeanFactory.autowireBeanProperties
 */
open class DependencyInjectionTestExecutionListener : AbstractTestExecutionListener() {

    companion object {

        /**
         * 标识它需要去进行二次注入的属性
         */
        private const val REINJECT_DEPENDENCIES_ATTRIBUTE =
            "com.wanna.framework.test.context.support.DependencyInjectionTestExecutionListener.reinjectDependencies"
    }

    /**
     * Order
     *
     * @return 2000
     */
    override fun getOrder(): Int = 2000

    /**
     * 准备TestInstance实例对象, 对[TestContext]当中的TestInstance去进行属性填充
     *
     * @param testContext TestContext
     */
    override fun prepareTestInstance(testContext: TestContext) {
        injectDependencies(testContext)
    }

    /**
     * 在`@Before`(JUnit5的`@BeforeEach`)方法执行之前, 如果必要的话, 需要对[TestContext]当中的TestInstance去进行再次属性填充
     *
     * @param testContext TestContext
     */
    override fun beforeTestMethod(testContext: TestContext) {
        // 如果必要的话, 在执行Test方法之前, 再去进行一次依赖注入
        if (testContext.getAttribute(REINJECT_DEPENDENCIES_ATTRIBUTE) == true) {
            injectDependencies(testContext)
        }
    }

    /**
     * 对于[TestContext]当中的Bean, 使用[AutowireCapableBeanFactory]去对它完成属性填充功能
     *
     * @param testContext TestContext
     */
    protected open fun injectDependencies(testContext: TestContext) {
        val bean = testContext.getTestInstance() ?: return
        val beanFactory = testContext.getApplicationContext().getAutowireCapableBeanFactory()

        // 利用BeanFactory去对bean的属性去进行填充
        beanFactory.autowireBeanProperties(bean, AUTOWIRE_NO, false)
        // 对该Bean去完成初始化
        beanFactory.initializeBean(bean, bean::class.java.name + ORIGINAL_INSTANCE_SUFFIX)

    }
}