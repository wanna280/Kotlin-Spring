package com.wanna.boot.devtools.restart

import com.wanna.boot.devtools.system.DevToolsEnablementDeducer
import com.wanna.framework.lang.Nullable
import java.net.URL

/**
 * [RestartInitializer]的默认实现, 提供要去监听的文件变化的URL, 只在"main"线程的情况下去进行初始化,
 * 并且应该确保使用的是main方法的方式去进行启动的方式才生效, 对于使用庞大的jar包和"test"工程的,
 * 我们都不应该去作为要去进行检查的情况(当然, 也可以自定义要添加进来的jar包)
 *
 * @see RestartInitializer
 * @see ChangeableUrls
 * @see com.wanna.boot.devtools.settings.DevToolsSettings.Companion.SETTINGS_RESOURCE_LOCATION
 */
open class DefaultRestartInitializer : RestartInitializer {

    /**
     * 获取DevTools的初始化的要去进行处理的初始化URL, 返回的这些URL当中的类, 将会被监听/重新加载
     *
     * @param thread thread
     * @return 如果需要启用DevTools, 那么return URLs; 如果不需要则return null
     */
    @Nullable
    override fun getInitialUrls(thread: Thread): Array<URL>? {
        // 如果给定的线程不是主线程, 那么return null
        if (!isMainThread(thread)) {
            return null
        }
        // 如果当前线程是被"test"框架启动的, 那么return null, 不应该去启用DevTools的功能
        if (!DevToolsEnablementDeducer.shouldEnable(thread)) {
            return null
        }
        // 如果当前线程确实是main方法, 也确实不是来自"test"框架, 那么需要去匹配合适的URL
        // 需要过滤出来符合"DevTools"的要求的URL列表去进行返回(默认情况下只会过滤文件夹出来, 当然也可以去进行自定义)
        return ChangeableUrls.fromClassLoader(thread.contextClassLoader).toList().toTypedArray()
    }

    /**
     * 判断给定的一个线程是否是main线程
     *
     * * 1.检查threadName是否为"main"
     * * 2.检查该线程的ClassLoader是否是一个DevelopmentClassLoader(是否是AppClassLoader)
     *
     * @param thread thread
     * @return 如果它是main线程, return true; 否则return false
     */
    protected open fun isMainThread(thread: Thread): Boolean =
        thread.name.equals("main") && isDevelopmentClassLoader(thread.contextClassLoader)

    /**
     * 判断一个ClassLoader是否是开发的ClassLoader
     *
     * @param classLoader classLoader
     * @return 默认情况下, 如果它是"AppClassLoader", return true; 否则return false
     */
    protected open fun isDevelopmentClassLoader(classLoader: ClassLoader): Boolean =
        classLoader::class.java.name.contains("AppClassLoader")
}