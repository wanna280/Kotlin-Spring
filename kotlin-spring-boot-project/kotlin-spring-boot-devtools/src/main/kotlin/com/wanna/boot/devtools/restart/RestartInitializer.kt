package com.wanna.boot.devtools.restart

import com.wanna.framework.lang.Nullable
import java.net.URL

/**
 * Restart的初始化器, 负责去提供要去进行监听的目录/要去进行重新加载的类的目录信息
 *
 * @see DefaultRestartInitializer
 */
fun interface RestartInitializer {

    companion object {
        /**
         * NONE实例
         */
        @JvmField
        val NONE = RestartInitializer { null }
    }

    /**
     * 给定一个具体的线程, 从该线程的ContextClassLoader当中去获取到用于Restart的初始化URL列表
     *
     * @param thread Thread
     * @return initialUrls(如果为null代表不去进行启用DevTools)
     */
    @Nullable
    fun getInitialUrls(thread: Thread): Array<URL>?
}