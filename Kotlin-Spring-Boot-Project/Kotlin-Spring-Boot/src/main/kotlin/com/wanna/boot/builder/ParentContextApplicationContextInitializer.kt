package com.wanna.boot.builder

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.Ordered

/**
 * 这是一个完成设置parentContext的设置的ApplicationContextInitializer
 *
 * @param parent parent ApplicationContext
 */
open class ParentContextApplicationContextInitializer(private val parent: ApplicationContext) :
    ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private var order: Int = Ordered.ORDER_HIGHEST

    open fun setOrder(order: Int) {
        this.order = order
    }

    override fun getOrder(): Int {
        return this.order
    }

    /**
     * 在对ApplicationContext去进行初始化时，设置parent ApplicationContext
     *
     * @param applicationContext child ApplicationContext
     */
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        applicationContext.setParent(this.parent)
    }
}