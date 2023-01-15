package com.wanna.cloud.context.refresh

import com.wanna.boot.ApplicationType
import com.wanna.boot.Banner
import com.wanna.boot.builder.SpringApplicationBuilder
import com.wanna.boot.env.EnvironmentPostProcessorApplicationListener
import com.wanna.cloud.bootstrap.BootstrapApplicationListener
import com.wanna.cloud.context.environment.EnvironmentChangeEvent
import com.wanna.cloud.context.scope.refresh.RefreshScope
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.environment.*

/**
 * ContextRefresher, 它可以去刷新ApplicationContext, 对环境信息去进行修改, 并去对RefreshScope当中的Bean去进行refreshAll; 
 * 它会在refresh时, 重新去构建一个SpringApplication, 目的是它可以重新完成启动, 并且会去应用ApplicationContext的初始化器; 
 * 而在初始化器会合并Bootstrap当中的, 因此会应用所有的PropertySourceLocator, 去完成从远程的配置中心去完成配置文件的重新加载
 *
 * @see RefreshScope
 */
open class ContextRefresher(
    private val applicationContext: ConfigurableApplicationContext,
    private val scope: RefreshScope
) {

    // 标准的PropertySource列表...
    private val standardSources = arrayOf(
        StandardEnvironment.SYSTEM_PROPERTY_PROPERTY_SOURCE_NAME,
        StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME
    )

    /**
     * 刷新RefreshScope和Environment
     */
    @Synchronized
    open fun refresh(): Set<String> {
        // first: 刷新Environment, 并通知所有的监听器, 对已经完成绑定的ConfigurationPropertiesBean去进行重新绑定(destroy&initialize)
        val keys = refreshEnvironment()

        // second: 刷新Scope, 将RefreshScope当中的全部Bean去进行destroy
        this.scope.refreshAll()
        return keys
    }

    /**
     * 刷新环境, 将环境当中的具体配置信息去进行更新...
     */
    @Synchronized
    open fun refreshEnvironment(): Set<String> {
        // 记录之前的所有属性值的k-v列表...
        val before = extract(this.applicationContext.getEnvironment().getPropertySources())

        // 将配置文件更新到环境当中, 构建一个新的SpringApplication, 为了实现在ApplicationContext初始化时去完成新的信息的加载...
        addConfigFilesToEnvironment()

        // 得到刷新换件之前/之后, 对于Environment当中的属性列表的变化情况
        val keys = change(before, extract(this.applicationContext.getEnvironment().getPropertySources())).keys

        // 发布环境改变事件...通知所有的监听器...并将变更的keys去进行一起传递过去
        this.applicationContext.publishEvent(EnvironmentChangeEvent(applicationContext, keys))
        return keys
    }


    /**
     * 这里会触发重新加载配置文件, 并将它设置到主程序的ApplicationContext当中去; 
     *
     * 使用的方式, 和之前构建Root Application一样, 因为Bootstrap的容器会有PropertySourceLocator, 它会应用给Root Application; 
     * Root Application在对ApplicationContext进行初始化时, 就会将应用所有的Locator, 去完成所有的配置文件的加载工作, 最终得到Environment; 
     * 在将构建的Root Application的Environment, 合并到之前的Root Environment当中
     */
    open fun addConfigFilesToEnvironment() {
        // 这里会触发构建一个新的容器, 并且该容器还会走BootstrapListener的逻辑...
        val builder = SpringApplicationBuilder().bannerMode(Banner.Mode.NO).web(ApplicationType.NONE).logStartupInfo(false)

        // 添加有用的ApplicationListener, 没用的就不要了, setListeners会去替换掉原来的SpringFactories当中的Listener列表
        val listeners = listOf(BootstrapApplicationListener(), EnvironmentPostProcessorApplicationListener())
        builder.setApplicationListeners(listeners)

        val context = builder.run()

        // 获取构建的Application的Environment以及MainApplicationContext的Environment当中的PropertySources
        val bootstrap = context.getEnvironment().getPropertySources()
        val environment = this.applicationContext.getEnvironment().getPropertySources()

        // 将Bootstrap当中的PropertySource, 全部都进行合并到AppEnvironment当中...
        bootstrap.forEach {
            if (environment.contains(it.name)) {
                environment.replace(it.name, it)
            } else {
                environment.addFirst(it)
            }
        }
    }

    /**
     * 得到刷新环境之前和之后, Environment当中的所有属性值的变化情况
     *
     * @param before before的属性值列表
     * @param after after的属性值列表
     * @return before和after之间的差距情况
     */
    private fun change(before: Map<String, Any>, after: Map<String, Any>): Map<String, Any> {
        val change = HashMap<String, Any>()
        after.forEach { (k, v) ->
            // 如果之前不包含k, 或者之前就包含了k但是之前之后的值不一样
            if (!before.containsKey(k) || before[k] != v) {
                change[k] = v
            }
        }
        return change
    }

    /**
     * 记录之前所有的PropertySource的属性值并去进行保存
     *
     * @param propertySources PropertySources
     * @return propertySources中维护的所有的PropertySource当中全部的属性值(k/v)列表
     */
    private fun extract(propertySources: MutablePropertySources): Map<String, Any> {
        val result = HashMap<String, Any>()

        // 逆向构建PropertySource, 保证最开始的PropertySource的优先级最高, 允许优先级高的去进行属性的替换
        val sources = ArrayList<PropertySource<*>>()
        propertySources.forEach { sources.add(0, it) }

        // 遍历所有的PropertySource, 如果它不是标准的PropertySource, 那么需要把它的全部属性值转移到result当中
        for (source in sources) {
            if (!standardSources.contains(source.name)) {
                extract(source, result)
            }
        }
        return result
    }

    /**
     * 记录某个PropertySource当中的全部的属性值并去进行保存(extract-->提取)
     *
     * @param propertySource PropertySource
     * @param result 将变更的属性值放入到result当中(输出参数)
     */
    private fun extract(propertySource: PropertySource<*>, result: MutableMap<String, Any>) {
        // 如果它是一个组合的PropertySource, 那么需要拿出里面的所有PropertySource去进行递归
        if (propertySource is CompositePropertySource) {
            // 逆向构建PropertySource, 保证最开始的PropertySource的优先级最高, 允许优先级高的去进行属性的替换
            val sources = ArrayList<PropertySource<*>>()
            propertySource.getPropertySources().forEach { sources.add(0, it) }
            // 遍历所有的PropertySource, 去进行递归
            sources.forEach { extract(it, result) }
            // 如果它是一个可以枚举的PropertySource, 那么讲它的所有k-v拿出来, 放入到result当中
        } else if (propertySource is EnumerablePropertySource<*>) {
            propertySource.getPropertyNames().forEach { result[it] = propertySource.getProperty(it)!! }
        }
    }
}