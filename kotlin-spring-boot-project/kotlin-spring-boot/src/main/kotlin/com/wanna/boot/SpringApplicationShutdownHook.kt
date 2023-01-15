package com.wanna.boot

import com.wanna.framework.context.ConfigurableApplicationContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * SpringApplication的ShutdownHook
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 */
open class SpringApplicationShutdownHook : Runnable {

    /**
     * ShutdownHook是否已经被添加的标识位, 可能存在有并发问题, 因此采用AtomicBoolean
     */
    private val shutdownHookAdded = AtomicBoolean()

    /**
     * 维护的所有的Spring应用的ApplicationContext
     */
    private val contexts = LinkedHashSet<ConfigurableApplicationContext>()

    /**
     * 往ShutdownHook当中去注册一个ApplicationContext
     *
     * @param context 要去进行注册的ApplicationContext
     */
    open fun registerApplicationContext(context: ConfigurableApplicationContext) {
        addRuntimeShutdownHookIfNecessary()
        synchronized(SpringApplication::class.java) {
            contexts += context
        }
    }

    /**
     * 如果必要的话, 需要去添加一个Runtime的ShutdownHook
     */
    private fun addRuntimeShutdownHookIfNecessary() {
        if (shutdownHookAdded.compareAndSet(false, true)) {
            addRuntimeShutdownHook()
        }
    }

    /**
     * 添加一个RuntimeShutdownHook
     */
    private fun addRuntimeShutdownHook() {
        Runtime.getRuntime().addShutdownHook(Thread(this, "SpringApplicationShutdownHook"))
    }

    override fun run() {

    }
}