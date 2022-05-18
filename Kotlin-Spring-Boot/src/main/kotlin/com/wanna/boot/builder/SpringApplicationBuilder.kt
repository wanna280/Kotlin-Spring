package com.wanna.boot.builder

import com.wanna.boot.ApplicationType
import com.wanna.boot.Banner
import com.wanna.boot.SpringApplication
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment

/**
 * 这是一个SpringApplication的Builder，支持去进行SpringApplication的构建
 *
 * @see SpringApplication
 */
open class SpringApplicationBuilder(vararg sources: Class<*>) {

    // SpringApplication
    private val application = SpringApplication(*sources)

    // parent SpringApplicationBuilder
    private var parent: SpringApplicationBuilder? = null

    // 已经创建好的ApplicationContext
    private var context: ConfigurableApplicationContext? = null

    open fun bannerMode(mode: Banner.Mode): SpringApplicationBuilder {
        this.application.setBannerMode(mode)
        return this
    }

    open fun setApplicationListeners(listeners:Collection<ApplicationListener<*>>) : SpringApplicationBuilder {
        this.application.setApplicationListeners(listeners)
        return this
    }

    open fun main(clazz: Class<*>?): SpringApplicationBuilder {
        this.application.setMainApplicationClass(clazz)
        return this
    }

    open fun getMainApplicationClass() : Class<*>? {
        return this.application.getMainApplicationClass()
    }

    open fun sources(vararg sources: Class<*>): SpringApplicationBuilder {
        this.application.addSources(*sources)
        return this
    }

    open fun environment(environment: ConfigurableEnvironment): SpringApplicationBuilder {
        this.application.setEnvironment(environment)
        return this
    }

    open fun logStartupInfo(logStartupInfo: Boolean): SpringApplicationBuilder {
        this.application.setLogStartupInfo(logStartupInfo)
        return this
    }

    open fun web(type: ApplicationType): SpringApplicationBuilder {
        this.application.setApplicationType(type)
        return this
    }

    /**
     * 设置parentBuilder，为parentApplicationContext的构建提供支持
     */
    open fun parent(builder: SpringApplicationBuilder) {
        this.parent = builder
    }

    /**
     * 通过sources去构建parentBuilder
     */
    open fun parent(vararg sources: Class<*>) {
        parent(SpringApplicationBuilder(*sources))
    }

    /**
     * run SpringApplication
     * 添加一个ApplicationContext的Initializer到容器当中，支持去对parentApplicationContext去进行设置
     *
     * @see ParentContextApplicationContextInitializer.initialize
     * @see ConfigurableApplicationContext.setParent
     */
    open fun run(vararg args: String): ConfigurableApplicationContext {
        if (this.parent != null) {
            this.application.addInitializer(ParentContextApplicationContextInitializer(this.parent!!.run(*args)))
        }
        this.context = application.run(*args)
        return this.context!!
    }

}