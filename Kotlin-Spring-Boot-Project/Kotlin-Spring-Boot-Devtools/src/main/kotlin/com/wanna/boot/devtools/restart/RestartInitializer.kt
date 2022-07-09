package com.wanna.boot.devtools.restart

import java.net.URL

/**
 * Restart的初始化器，负责去提供要去进行监听的目录/要去进行重新加载的类的目录信息
 *
 * @see DefaultRestartInitializer
 */
interface RestartInitializer {
    /**
     * 给定一个具体的线程，去获取到初始化的URL
     *
     * @param thread Thread
     * @return initialUrls(如果为null代表不去进行启用DevTools)
     */
    @com.wanna.framework.lang.Nullable
    fun getInitialUrls(thread: Thread): Array<URL>?
}