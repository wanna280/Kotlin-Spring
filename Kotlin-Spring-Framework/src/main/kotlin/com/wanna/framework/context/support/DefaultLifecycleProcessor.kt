package com.wanna.framework.context.support

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.Lifecycle
import com.wanna.framework.context.LifecycleProcessor
import com.wanna.framework.beans.BeanFactoryAware

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

    /**
     * 获取LifecycleBean列表，在寻找的过程当中，需要去掉本身，如果本身不去掉，肯定**会出现SOF**
     */
    private fun getLifecycleBeans(): List<Lifecycle> {
        val lifecycles = beanFactory?.getBeansForType(Lifecycle::class.java)
        val lifecycleBeans = ArrayList<Lifecycle>()
        lifecycles?.forEach { (_, bean) ->
            if (bean != this) {
                lifecycleBeans += bean
            }
        }
        return lifecycleBeans
    }

    private fun stopBeans() {
        val lifecycleBeans = getLifecycleBeans()
        lifecycleBeans.forEach(Lifecycle::stop)
    }
}