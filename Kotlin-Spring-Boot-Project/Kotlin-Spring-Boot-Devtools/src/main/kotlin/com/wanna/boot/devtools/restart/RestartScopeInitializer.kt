package com.wanna.boot.devtools.restart

import com.wanna.boot.ApplicationContextInitializer
import com.wanna.framework.beans.factory.ObjectFactory
import com.wanna.framework.beans.factory.config.Scope
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * RestartScopeInitializer，负责在SpringBoot启动时，去注册一个RestartScope
 *
 * @see RestartScope
 * @see com.wanna.boot.devtools.restart.RestartScope
 */
class RestartScopeInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        applicationContext.getBeanFactory().registerScope("restart", RestartScope())
    }

    /**
     * RestartScope，自定义一个Spring的Scope
     *
     * @see com.wanna.boot.devtools.restart.RestartScope
     */
    private class RestartScope : Scope {
        @Suppress("UNCHECKED_CAST")
        override fun get(beanName: String, factory: ObjectFactory<*>) =
            Restarter.getInstance()!!.getOrAddAttribute(beanName, factory as ObjectFactory<Any>)

        override fun registerDestructionCallback(name: String, callback: Runnable) {

        }

        override fun remove(name: String) = Restarter.getInstance()!!.removeAttribute(name)
    }
}