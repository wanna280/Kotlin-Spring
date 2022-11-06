package com.wanna.framework.test.context.event

import com.wanna.framework.beans.factory.ObjectFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.support.AbstractApplicationContext
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.test.context.TestContext
import com.wanna.framework.test.context.TestExecutionListener
import com.wanna.framework.test.context.support.AbstractTestExecutionListener
import java.io.Serializable

/**
 * [ApplicationEvents]的[TestExecutionListener]，监听Test方法的执行流程，
 * 往[ApplicationContext]当中去注册[ApplicationEventsApplicationListener]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
open class ApplicationEventsTestExecutionListener : AbstractTestExecutionListener() {

    companion object {
        /**
         * 是否需要记录[ApplicationEvents]的一个属性标识
         */
        private const val RECORD_APPLICATION_EVENTS =
            "com.wanna.framework.test.context.event.ApplicationEventsTestExecutionListener.recordApplicationEvents"

        /**
         * 操作ApplicationEvents的锁
         */
        private val applicationEventsMonitor = Any()
    }

    override fun getOrder(): Int = 1800

    /**
     * 在准备testInstance时，先去往[ApplicationEventsHolder]当中去注册一个[ApplicationEvents]
     *
     * @param testContext TestContext
     */
    override fun prepareTestInstance(testContext: TestContext) {
        if (recordApplicationEvents(testContext)) {
            registerListenerAndResolvableDependencyIfNecessary(testContext.getApplicationContext())
            ApplicationEventsHolder.registerApplicationEvents()
        }
    }

    /**
     * 在Test方法执行之前，如果必要的话，先去注册一下[ApplicationEvents]到[ApplicationEventsHolder]当中
     *
     * @param testContext TestContext
     */
    override fun beforeTestExecution(testContext: TestContext) {
        if (recordApplicationEvents(testContext)) {
            ApplicationEventsHolder.registerApplicationEventsIfNecessary()
        }
    }

    /**
     * 在Test方法执行之后，需要清理[ApplicationEventsHolder]当中的[ApplicationEvents]
     */
    override fun afterTestExecution(testContext: TestContext) {
        if (recordApplicationEvents(testContext)) {
            ApplicationEventsHolder.unregisterApplicationEvents()
        }
    }

    /**
     * * 1.注册一个[ApplicationEventsApplicationListener]到[ApplicationContext]当中
     * * 2.给BeanFactory当中去为ApplicationEvents注册一个ResolvableDependency
     *
     * @param applicationContext ApplicationContext
     */
    private fun registerListenerAndResolvableDependencyIfNecessary(applicationContext: ApplicationContext) {
        if (applicationContext !is AbstractApplicationContext) {
            throw IllegalStateException("只有AbstractApplicationContext才支持去提供Test")
        }
        synchronized(applicationEventsMonitor) {

            // 检查一下之前是否已经注册过了ApplicationEventsApplicationListener？
            val notAlreadyRegistered =
                applicationContext.getApplicationListeners().stream()
                    .map { it::class.java }
                    .noneMatch { it == ApplicationEventsApplicationListener::class.java }

            // 如果之前还没注册过的话，那么我们需要去注册ApplicationEventsApplicationListener
            if (notAlreadyRegistered) {
                // 添加一个ApplicationEventsApplicationListener
                applicationContext.addApplicationListener(ApplicationEventsApplicationListener())

                // 注册一个[ResolvableDependency]，提供@Autowired的自动注入
                applicationContext.getBeanFactory()
                    .registerResolvableDependency(ApplicationEvents::class.java, ApplicationEventsObjectFactory())
            }
        }
    }

    /**
     * 检查一下是否需要记录[ApplicationEvents]？
     *
     * @param testContext TestContext
     * @return 如果testClass上标注空[RecordApplicationEvents]注解，那么return true；否则return false
     */
    private fun recordApplicationEvents(testContext: TestContext): Boolean {
        return AnnotatedElementUtils.getMergedAnnotation(
            testContext.getTestClass(),
            RecordApplicationEvents::class.java
        ) != null
    }

    /**
     * 暴露当前线程的[ApplicationEvents]的[ObjectFactory]
     *
     * @see RecordApplicationEvents
     */
    private class ApplicationEventsObjectFactory : ObjectFactory<ApplicationEvents>, Serializable {
        override fun getObject(): ApplicationEvents = ApplicationEventsHolder.getRequiredApplicationEvents()
        override fun toString(): String = "Current ApplicationEvents"
    }
}