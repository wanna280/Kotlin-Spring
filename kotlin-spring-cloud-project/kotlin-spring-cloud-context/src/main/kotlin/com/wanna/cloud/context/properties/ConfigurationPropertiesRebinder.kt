package com.wanna.cloud.context.properties

import com.wanna.cloud.context.environment.EnvironmentChangeEvent
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.stereotype.Component
import com.wanna.boot.context.properties.ConfigurationPropertiesBean

/**
 * 它是一个[ApplicationListener], 负责监听环境改变的事件([EnvironmentChangeEvent]),
 * 当EnvironmentChangeEvent这个事件发布时, 将会对RefreshScope内的全部Bean去进行rebind;
 * 它需要去负责将所有的RefreshScope内的Bean全部去完成Rebind, 首先利用BeanFactory对Bean去进行destroy, 并重新对Bean去完成初始化工作
 *
 * @param beans 维护了所有的要去进行绑定的[ConfigurationPropertiesBean]的列表
 * @see ConfigurationPropertiesBeans
 */
@Component
open class ConfigurationPropertiesRebinder(private val beans: ConfigurationPropertiesBeans) : ApplicationContextAware,
    ApplicationListener<EnvironmentChangeEvent> {

    /**
     * ApplicationContext
     */
    private var applicationContext: ApplicationContext? = null

    /**
     * 设置ApplicationContext
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    open fun getApplicationContext(): ApplicationContext =
        this.applicationContext ?: throw IllegalStateException("ApplicationContext还未完成初始化, 无法获取")

    /**
     * 监听环境改变事件, 负责对维护的@ConfigurationProperties的Bean去进行rebind
     *
     * @param event Spring的Environment环境改变的事件
     */
    override fun onApplicationEvent(event: EnvironmentChangeEvent) {
        if (event.source == getApplicationContext()) {
            rebind()
        }
    }

    /**
     * 将RefreshScope内的所有的[ConfigurationPropertiesBean]去进行重新绑定
     */
    open fun rebind() {
        this.beans.getBeanNames().forEach(this::rebind)
    }

    /**
     * 将RefreshScope内的全部Bean去进行重新绑定
     * 交给BeanFactory去进行初始化时, 就会走@ConfigurationProperties的重新绑定;
     *
     * @param name 要去进行绑定的beanName
     * @return 如果绑定成功, return true; 否则return false
     */
    open fun rebind(name: String): Boolean {
        // 如果该beanName都不在给定的ConfigurationPropertiesBean当中, 那么return false
        if (!this.beans.getBeanNames().contains(name)) {
            return false
        }

        // 从ApplicationContext当中去获取到Bean实例, 以及用于去提供Bean的初始化工作的AutowireCapableBeanFactory
        val bean = getApplicationContext().getBean(name)
        val beanFactory = getApplicationContext().getAutowireCapableBeanFactory()


        // destroy Bean and reinitialize it...
        beanFactory.destroy(bean)
        beanFactory.initializeBean(bean, name)
        return true
    }
}