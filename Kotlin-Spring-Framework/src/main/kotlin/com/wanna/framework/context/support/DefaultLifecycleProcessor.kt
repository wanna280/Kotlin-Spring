package com.wanna.framework.context.support

import com.wanna.framework.context.BeanFactory
import com.wanna.framework.context.ConfigurableListableBeanFactory
import com.wanna.framework.context.Lifecycle
import com.wanna.framework.context.LifecycleProcessor
import com.wanna.framework.context.aware.BeanFactoryAware

/**
 * 这是一个默认的Lifecycle的处理器实现
 *
 * @see Lifecycle
 * @see LifecycleProcessor
 */
class DefaultLifecycleProcessor : LifecycleProcessor, BeanFactoryAware {

    private var beanFactory: ConfigurableListableBeanFactory? = null

    private var running: Boolean = false

    override fun start() {
        startBeans()
        this.running = true
    }

    override fun stop() {
        stopBeans()
        this.running = false
    }

    override fun isRunning(): Boolean {
        return this.running
    }

    override fun onRefresh() {
        start()
    }

    override fun onClose() {
        stop()
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory as ConfigurableListableBeanFactory
    }

    fun getBeanFactory(): ConfigurableListableBeanFactory? = this.beanFactory

    private fun startBeans() {
        val lifecycleBeans = getLifecycleBeans()
        lifecycleBeans.forEach(Lifecycle::start)
    }

    private fun getLifecycleBeans(): List<Lifecycle> {
        val lifecycles = beanFactory?.getBeansForType(Lifecycle::class.java)
        return ArrayList(lifecycles!!.values)
    }

    private fun stopBeans() {
        val lifecycleBeans = getLifecycleBeans()
        lifecycleBeans.forEach(Lifecycle::stop)
    }
}