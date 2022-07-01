package com.wanna.framework.context.support

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.Lifecycle
import com.wanna.framework.context.LifecycleProcessor
import com.wanna.framework.beans.BeanFactoryAware

/**
 * 这是一个默认的Lifecycle的处理器实现，它支持拿出容器当中的所有Lifecycle类型的Bean，去完成启动(start)和停止(stop)；
 * 对于Web服务器这种情况来说，就可以注册一个Lifecycle到容器当中，在这里完成Lifecycle的回调，从而去启动Web服务器
 *
 * @see Lifecycle
 * @see LifecycleProcessor
 */
open class DefaultLifecycleProcessor : LifecycleProcessor, BeanFactoryAware {

    private lateinit var beanFactory: ConfigurableListableBeanFactory

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

    fun getBeanFactory(): ConfigurableListableBeanFactory = this.beanFactory

    /**
     * 启动所有的Bean，拿出容器当中的所有的Lifecycle，去执行start
     */
    private fun startBeans() {
        getLifecycleBeans().forEach(Lifecycle::start)
    }

    /**
     * 关闭所有的Bean，拿出容器当中的所有的Lifecycle，去执行stop
     */
    private fun stopBeans() {
        getLifecycleBeans().forEach(Lifecycle::stop)
    }

    /**
     * 获取LifecycleBean列表
     *
     * Note: 在寻找的过程当中，需要去掉本身，如果本身不去掉，肯定**会出现SOF**
     *
     * @return 容器当中找到的Lifecycle的Bean的列表
     */
    private fun getLifecycleBeans(): List<Lifecycle> {
        val lifecycles = beanFactory.getBeansForType(Lifecycle::class.java)
        val lifecycleBeans = ArrayList<Lifecycle>()
        lifecycles.forEach { (_, bean) ->
            if (bean != this) {
                lifecycleBeans += bean
            }
        }
        return lifecycleBeans
    }
}