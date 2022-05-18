package com.wanna.cloud.context.refresh

import com.wanna.boot.ApplicationType
import com.wanna.boot.Banner
import com.wanna.boot.builder.SpringApplicationBuilder
import com.wanna.boot.env.EnvironmentPostProcessorApplicationListener
import com.wanna.cloud.bootstrap.BootstrapApplicationListener
import com.wanna.cloud.context.environment.EnvironmentChangeEvent
import com.wanna.cloud.context.scope.refresh.RefreshScope
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.environment.MutablePropertySources

/**
 * ContextRefresher，它可以去刷新ApplicationContext，对环境信息去进行修改，并去对RefreshScope当中的Bean去进行refreshAll；
 * 它会在refresh时，重新去构建一个SpringApplication，目的是它可以重新完成启动，并且会去应用ApplicationContext的初始化器；
 * 而在初始化器会合并Bootstrap当中的，因此会应用所有的PropertySourceLocator，去完成从远程的配置中心去完成配置文件的重新加载
 *
 * @see RefreshScope
 */
open class ContextRefresher(
    private val applicationContext: ConfigurableApplicationContext,
    private val scope: RefreshScope
) {

    @Synchronized
    open fun refresh(): Set<String> {
        val keys = refreshEnvironment()
        this.scope.refreshAll()
        return keys
    }

    @Synchronized
    open fun refreshEnvironment(): Set<String> {
        // 记录之前的...
        val before = extract(this.applicationContext.getEnvironment().getPropertySources())

        // 将配置文件更新到环境当中，构建一个新的SpringApplication，为了实现在ApplicationContext初始化时去完成新的信息的加载...
        addConfigFilesToEnvironment()

        // 得到变化的属性...
        val keys = change(before, extract(this.applicationContext.getEnvironment().getPropertySources())).keys

        // 发布环境改变事件...通知所有的监听器...
        this.applicationContext.publishEvent(EnvironmentChangeEvent(applicationContext, keys))
        return keys
    }

    open fun extract(propertySources: MutablePropertySources): Map<String, Any> {

        return emptyMap()
    }

    /**
     * 这里会触发重新加载配置文件，并将它设置到主程序的ApplicationContext当中去；
     * 使用的方式，和之前构建Root Application一样，因为Bootstrap的容器会有PropertySourceLocator，它会应用给Root Application；
     * Root Application在对ApplicationContext进行初始化时，就会将应用所有的Locator，去完成所有的配置文件的加载工作，最终得到Environment；
     * 在将构建的Root Application的Environment，合并到之前的Root Environment当中
     */
    open fun addConfigFilesToEnvironment() {

        // 这里会触发构建一个新的容器，并且该容器还会走BootstrapListener的逻辑...
        val builder = SpringApplicationBuilder().bannerMode(Banner.Mode.NO).web(ApplicationType.NONE)

        // 添加有用的ApplicationListener，没用的就不要了，setListeners会去替换掉原来的SpringFactories当中的Listener列表
        val listeners = listOf(BootstrapApplicationListener(), EnvironmentPostProcessorApplicationListener())
        builder.setApplicationListeners(listeners)

        var context: ConfigurableApplicationContext
        // run
        try {
            context = builder.run()
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }


        // 获取构建的SpringApplication的Environment以及MainApplicationContext
        val bootstrap = context.getEnvironment().getPropertySources()
        val environment = this.applicationContext.getEnvironment().getPropertySources()


        // 将Bootstrap当中的PropertySource合并到AppEnvironment当中...
        bootstrap.forEach {
            if (environment.contains(it.name)) {
                environment.replace(it.name, it)
            } else {
                environment.addFirst(it)
            }
        }
    }

    open fun change(before: Map<String, Any>, after: Map<String, Any>): Map<String, Any> {

        return emptyMap()
    }
}