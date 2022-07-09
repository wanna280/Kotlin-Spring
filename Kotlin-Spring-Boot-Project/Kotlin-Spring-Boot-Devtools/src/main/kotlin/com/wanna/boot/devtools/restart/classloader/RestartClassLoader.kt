package com.wanna.boot.devtools.restart.classloader

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.URLClassLoader

/**
 * 负责处理"SpringBoot-Devtools"的重启的ClassLoader
 *
 * @param urls ClassLoader需要负责加载的URL列表
 * @param parent parent ClassLoader
 * @param logger Logger
 */
open class RestartClassLoader(
    urls: Array<URL>,
    parent: ClassLoader,
    private val updatedFiles: ClassLoaderFileRepository = ClassLoaderFileRepository.NONE,
    private val logger: Logger = LoggerFactory.getLogger(RestartClassLoader::class.java)
) : URLClassLoader(urls, parent) {

    init {
        if (logger.isDebugEnabled) {
            logger.debug("正在创建RestartClassLoader [${toString()}]")
        }
    }

    /**
     * 重写父类的loadClass方法，去完成自定义的加载类的逻辑，
     * 我们优先使用当前的ClassLoader去完成要去进行热加载的类，
     * 对于不用热加载的类，我们直接交给parent去进行加载
     *
     * @param name className
     * @param resolve 是否要完成链接？
     * @return 根据className加载到的类
     */
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        synchronized(getClassLoadingLock(name)) {
            // 1. 尝试去检查缓存，如果缓存当中已经有了，就不必去进行类加载了
            var loadedClass = findLoadedClass(name)

            // 2. 如果缓存当中没有，那么我们就得尝试去进行加载了
            if (loadedClass == null) {
                // 2.1 尝试去当前ClassLoader去进行加载，看是否能加载到？
                loadedClass = try {
                    findClass(name)
                } catch (ex: ClassNotFoundException) {
                    // 2.2 如果当前ClassLoader加载不到，那么尝试交给parent去进行加载
                    Class.forName(name, false, parent)
                }
            }
            // 如果要去完成初始化，需要去对该类去完成链接工作
            if (resolve) {
                resolveClass(loadedClass)
            }
            return loadedClass
        }
    }
}