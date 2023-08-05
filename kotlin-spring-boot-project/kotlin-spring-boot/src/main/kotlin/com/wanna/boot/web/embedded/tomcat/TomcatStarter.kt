package com.wanna.boot.web.embedded.tomcat

import com.wanna.boot.web.servlet.ServletContextInitializer
import javax.servlet.ServletContainerInitializer
import javax.servlet.ServletContext

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
class TomcatStarter(private val initializers: Array<ServletContextInitializer>) : ServletContainerInitializer {

    override fun onStartup(classes: MutableSet<Class<*>>, servletContext: ServletContext) {
        for (initializer in initializers) {
            initializer.onStartup(servletContext)
        }
    }
}