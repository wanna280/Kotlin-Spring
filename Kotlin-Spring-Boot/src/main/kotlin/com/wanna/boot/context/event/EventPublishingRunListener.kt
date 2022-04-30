package com.wanna.boot.context.event

import com.wanna.boot.ConfigurableBootstrapContext
import com.wanna.boot.SpringApplication
import com.wanna.boot.SpringApplicationRunListener
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.event.ApplicationEventMulticaster
import com.wanna.framework.context.event.SimpleApplicationEventMulticaster
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * 这是一个Spring的事件发布的运行监听器，它负责回调所有的监听器
 */
open class EventPublishingRunListener(
    private val springApplication: SpringApplication, private val args: Array<String>
) : SpringApplicationRunListener, Ordered {

    private var order: Int = 0

    // 这是一个事件多拨器，可以完成ApplicationEvent的事件发布
    private val initialMulticaster: ApplicationEventMulticaster = SimpleApplicationEventMulticaster()

    init {
        // 将SpringApplication当中的ApplicationListener全部转移到事件多拨器当中
        springApplication.getListeners().forEach { initialMulticaster.addApplicationListener(it) }
    }

    override fun getOrder(): Int {
        return this.order
    }

    open fun setOrder(order: Int) {
        this.order = order
    }

    /**
     * SpringApplication已经正在启动当中了
     *
     * @see SpringApplication.run
     */
    override fun starting(bootstrapContext: ConfigurableBootstrapContext) {
        this.initialMulticaster.multicastEvent(ApplicationStartingEvent(bootstrapContext, springApplication, args))
    }

    /**
     * SpringApplication的环境信息已经准备好了
     *
     * @see SpringApplication.prepareEnvironment
     * @see SpringApplication.run
     */
    override fun environmentPrepared(
        bootstrapContext: ConfigurableBootstrapContext, configurableEnvironment: ConfigurableEnvironment
    ) {
        this.initialMulticaster.multicastEvent(
            ApplicationEnvironmentPreparedEvent(bootstrapContext, springApplication, args, configurableEnvironment)
        )
    }

    /**
     * SpringApplication已经完成了ApplicationContext的创建工作
     *
     * @see SpringApplication.prepareContext
     * @see SpringApplication.run
     */
    override fun contextPrepared(context: ConfigurableApplicationContext) {
        this.initialMulticaster.multicastEvent(ApplicationContextInitializedEvent(context, springApplication, args))
    }

    /**
     * 在这里，SpringApplication的ApplicationContext已经完成了创建以及配置类的注册(还未刷新)，因此可以往ApplicationContext当中注册ApplicationListener了
     * 我们将已经保存的所有的SpringApplication的ApplicationListener全部转移到ApplicationContext当中去，也就是设置到applicationListeners列表当中去
     * 但是此时ApplicationContext还没有发布事件的能力，因为它还未完成刷新(refresh)，因此此时仍旧使用initialMulticaster去进行发布事件而不是使用ApplicationContext去发布
     *
     * @see com.wanna.framework.context.support.AbstractApplicationContext.refresh
     * @see com.wanna.framework.context.support.AbstractApplicationContext.applicationListeners
     * @see SpringApplication.prepareContext
     */
    override fun contextLoaded(context: ConfigurableApplicationContext) {
        springApplication.getListeners().forEach {
            if (it is ApplicationContextAware) {
                it.setApplicationContext(context)
            }
            context.addApplicationListener(it)
        }
        this.initialMulticaster.multicastEvent(ApplicationPreparedEvent(context, springApplication, args))
    }

    /**
     * 此时SpringApplication的ApplicationContext已经完成了刷新，可以使用ApplicationContext去完成事件的发布了
     *
     * @see com.wanna.framework.context.support.AbstractApplicationContext.refresh
     * @see SpringApplication.run
     * @see com.wanna.framework.context.support.AbstractApplicationContext.applicationListeners
     */
    override fun started(context: ConfigurableApplicationContext) {
        context.publishEvent(ApplicationStartedEvent(springApplication, context, args))
    }

    /**
     * 此时SpringApplication已经处于运行当中了，可以使用ApplicationContext去完成事件的发布
     *
     * @see com.wanna.framework.context.support.AbstractApplicationContext.refresh
     * @see com.wanna.framework.context.support.AbstractApplicationContext.applicationListeners
     */
    override fun running(context: ConfigurableApplicationContext) {
        context.publishEvent(ApplicationReadyEvent(context, springApplication, args))
    }

    /**
     * SpringApplication启动失败，有可能ApplicationContext已经创建，也有可能ApplicationContext还没完成创建
     *
     * @see SpringApplication.run
     * @see SpringApplication.handleRunException
     */
    override fun failed(context: ConfigurableApplicationContext?, ex: Throwable) {
        if (context == null) {
            this.initialMulticaster.multicastEvent(ApplicationFailedEvent(context, springApplication, args, ex))
        } else {
            context.publishEvent(ApplicationFailedEvent(context, springApplication, args, ex))
        }
    }
}