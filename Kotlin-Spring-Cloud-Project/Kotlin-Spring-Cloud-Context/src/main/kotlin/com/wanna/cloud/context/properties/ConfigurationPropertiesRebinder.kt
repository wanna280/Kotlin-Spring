package com.wanna.cloud.context.properties

import com.wanna.cloud.context.environment.EnvironmentChangeEvent
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.stereotype.Component

/**
 * 它是一个ApplicationListener，负责监听环境改变的事件，当EnvironmentChangeEvent发布时，将会对RefreshScope内的全部Bean去进行rebind
 * 负责将所有的RefreshScope内的Bean全部去完成Rebind，首先利用BeanFactory对Bean去进行destroy，并重新对Bean去完成初始化工作
 *
 * @param beans 维护了ConfigurationPropertiesBean的列表
 * @see ConfigurationPropertiesBeans
 */
@Component
open class ConfigurationPropertiesRebinder(private val beans: ConfigurationPropertiesBeans) : ApplicationContextAware,
    ApplicationListener<EnvironmentChangeEvent> {

    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * 监听环境改变事件，负责对维护的@ConfigurationProperties的Bean去进行rebind
     */
    override fun onApplicationEvent(event: EnvironmentChangeEvent) {
        if (event.source == this.applicationContext) {
            rebind()
        }
    }

    /**
     * 将RefreshScope内的所有的ConfigurationPropertiesBean去进行重新绑定
     */
    open fun rebind() {
        this.beans.getBeanNames().forEach(this::rebind)
    }

    /**
     * 将RefreshScope内的全部Bean去进行重新绑定
     * 交给BeanFactory去进行初始化时，就会走@ConfigurationProperties的重新绑定；
     *
     * @param name 要去进行绑定的beanName
     * @return 如果绑定成功，return true；否则return false
     */
    open fun rebind(name: String): Boolean {
        // 如果该beanName都不在给定的ConfigurationPropertiesBean当中，那么return false
        if (!this.beans.getBeanNames().contains(name)) {
            return false
        }
        val bean = applicationContext.getBean(name)
        val beanFactory = applicationContext.getAutowireCapableBeanFactory()


        // destroy Bean and reinitialize it...
        beanFactory.destroy(bean)
        beanFactory.initializeBean(bean, name)
        return true
    }
}