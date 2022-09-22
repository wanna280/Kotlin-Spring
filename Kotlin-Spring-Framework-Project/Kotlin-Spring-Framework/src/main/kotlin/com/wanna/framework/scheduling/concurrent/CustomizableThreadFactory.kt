package com.wanna.framework.scheduling.concurrent

import com.wanna.framework.util.CustomizableThreadCreator
import java.util.concurrent.ThreadFactory

/**
 * 可以支持去进行自定义的ThreadFactory
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/19
 */
open class CustomizableThreadFactory() : CustomizableThreadCreator(), ThreadFactory {

    /**
     * 提供一个支持去进行设置ThreadNamePrefix的构造器
     *
     * @param threadNamePrefix ThreadNamePrefix
     */
    constructor(threadNamePrefix: String) : this() {
        super.setThreadNamePrefix(threadNamePrefix)
    }


    /**
     * 来自ThreadFactory的方法，用于为线程池去创建一个Thread线程对象的工厂方法
     *
     * @param r 线程要去进行执行的任务
     * @return 创建好的Thread线程对象
     */
    override fun newThread(r: Runnable) = createThread(r)
}