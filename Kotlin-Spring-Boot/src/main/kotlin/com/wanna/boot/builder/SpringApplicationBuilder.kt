package com.wanna.boot.builder

import com.wanna.boot.SpringApplication
import com.wanna.framework.context.ConfigurableApplicationContext

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
    open fun run(args: Array<String>): ConfigurableApplicationContext {
        if (this.parent != null) {
            this.application.addInitializer(ParentContextApplicationContextInitializer(this.parent!!.run(args)))
        }
        this.context = application.run(*args)
        return this.context!!
    }

}