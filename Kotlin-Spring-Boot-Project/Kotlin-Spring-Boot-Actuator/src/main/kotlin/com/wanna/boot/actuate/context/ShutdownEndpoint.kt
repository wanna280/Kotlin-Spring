package com.wanna.boot.actuate.context

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.WriteOperation
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * 对外提供关闭ApplicationContext的Endpoint
 */
@Endpoint("shutdown")
open class ShutdownEndpoint(private val applicationContext: ConfigurableApplicationContext? = null) {
    companion object {
        private val NO_CONTEXT_MESSAGE = mapOf("message" to "No Application to Close...")
        private val SHUTDOWN_MESSAGE = mapOf("message" to "Shut down, bye...")
    }

    /**
     * 关闭ApplicationContext
     */
    @WriteOperation
    open fun shutdown(): Map<String, String> {
        if (this.applicationContext == null) {
            return NO_CONTEXT_MESSAGE
        }
        try {
            return SHUTDOWN_MESSAGE
        } finally {
            // create a Thread to perform Shutdown
            val thread = Thread(this::performShutdown)
            thread.contextClassLoader = javaClass.classLoader
            thread.start()
        }
    }

    /**
     * 执行真正的shutdown操作，关闭ApplicationContext
     */
    private fun performShutdown() {
        try {
            Thread.sleep(500L)  // 先等一会，保证当前的业务可以正常去进行
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()  // interrupt
        }
        this.applicationContext?.close()  // 关闭ApplicationContext
    }
}