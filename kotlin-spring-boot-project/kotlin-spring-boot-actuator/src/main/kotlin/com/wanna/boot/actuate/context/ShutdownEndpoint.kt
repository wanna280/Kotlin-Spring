package com.wanna.boot.actuate.context

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.WriteOperation
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * 对外提供关闭当前的Spring ApplicationContext的Endpoint
 *
 * @param applicationContext 需要去进行关闭的ApplicationContext
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
        // 如果没有ApplicationContext, 发送没有"No Application to Close..."的消息
        applicationContext ?: return NO_CONTEXT_MESSAGE

        // 如果有ApplicationContext, 那么发送"Shut down, bye..."的消息
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
     * 执行真正的shutdown操作, 关闭ApplicationContext
     */
    private fun performShutdown() {
        try {
            Thread.sleep(500L)  // 先等一会, 保证当前的业务可以正常去进行
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()  // interrupt
        }
        this.applicationContext?.close()  // 关闭ApplicationContext
    }
}