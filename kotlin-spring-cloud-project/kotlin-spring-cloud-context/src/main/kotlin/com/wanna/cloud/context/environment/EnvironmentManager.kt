package com.wanna.cloud.context.environment

import com.wanna.framework.context.ApplicationEventPublisherAware
import com.wanna.framework.context.event.ApplicationEventPublisher
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.MapPropertySource
import com.wanna.framework.lang.Nullable

/**
 * 可以让已经运行当中的应用的Environment当中的配置信息可以发生改变的入口entrypoint,
 * 允许Environment内部的属性新增, 也允许修改Environment当中的已经存在的属性, 仅仅通过
 * 往Environment当中添加一个最高优先级的PropertySource去进行实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/5/29
 */
@Suppress("UNCHECKED_CAST")
@Component
open class EnvironmentManager(private val environment: ConfigurableEnvironment) : ApplicationEventPublisherAware {

    companion object {

        /**
         * EnvironmentManager需要添加的PropertySource的name
         */
        private const val MANAGER_PROPERTY_SOURCE = "manager";
    }


    /**
     * 配置信息的Map
     */
    private var configMap: MutableMap<String, Any> = LinkedHashMap()


    /**
     * ApplicationEvent的事件发布器
     */
    private var publisher: ApplicationEventPublisher? = null

    init {
        val propertySources = environment.getPropertySources()
        // 如果已经存在有manager的PropertySource, 使用它的configMap作为configMap
        if (propertySources.contains(MANAGER_PROPERTY_SOURCE)) {
            val map = propertySources.get(MANAGER_PROPERTY_SOURCE)!!.source as MutableMap<String, Any>
            this.configMap = map
        }
    }

    override fun setApplicationEventPublisher(publisher: ApplicationEventPublisher) {
        this.publisher = publisher
    }

    /**
     * 重设configMap, 将configMap当中的内容全部清空掉
     *
     * @return 在清空之前, configMap当中的配置信息内容
     */
    open fun reset(): Map<String, Any> {
        val result = LinkedHashMap(configMap)
        if (configMap.isNotEmpty()) {
            configMap.clear()
            publish(EnvironmentChangeEvent(publisher ?: Any(), result.keys))
        }

        return result
    }

    /**
     * 修改Environment当中某个属性值
     *
     * @param name name
     * @param value value
     */
    open fun setProperty(name: String, value: String) {
        if (!this.environment.getPropertySources().contains(MANAGER_PROPERTY_SOURCE)) {
            synchronized(this.configMap) {
                if (!this.environment.getPropertySources().contains(MANAGER_PROPERTY_SOURCE)) {
                    val source = MapPropertySource(MANAGER_PROPERTY_SOURCE, configMap)
                    // 将新添加的PropertySource去添加到first位置, 为了最高优先级
                    this.environment.getPropertySources().addFirst(source)
                }
            }
        }

        // 如果属性值发生了变化的话, 那么需要修改configMap当中的值, 并发布环境已经改变的事件...
        if (this.environment.getProperty(name) != value) {
            this.configMap[name] = value
            publish(EnvironmentChangeEvent(publisher ?: Any(), setOf(name)))
        }
    }

    /**
     * 从Environment当中去获取属性值
     *
     * @param name 属性名
     * @return 获取到的属性值
     */
    @Nullable
    open fun getProperty(name: String): String? {
        return environment.getProperty(name)
    }

    /**
     * 发布环境已经发生了改变的事件
     *
     * @param event event
     */
    private fun publish(event: EnvironmentChangeEvent) {
        publisher?.publishEvent(event)
    }

}