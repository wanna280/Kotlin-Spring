package com.wanna.boot.devtools.restart.classloader

import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory
import java.net.URL
import java.net.URLClassLoader

/**
 * 负责处理"SpringBoot-Devtools"的重启的ClassLoader
 *
 * @param urls 当前的RestartClassLoader需要去进行负责加载的URL列表, 对于不在这个url当中的类, 交给parentClassLoader去进行加载
 * @param parent parent ClassLoader(如果当前的类加载器找不到要去进行加载的类, 直接交给父类去进行加载)
 * @param updatedFiles 发生变更的文件, 它可以保证被优先去进行加载
 * (因为有可能当前是运行到jar包当中, 但是具体的类的资源是来自于网络等渠道, 需要保证它拥有更高的优先级)
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
            logger.debug("正在创建RestartClassLoader[${this}]")
        }
    }

    /**
     * 重写父类的loadClass方法, 去完成自定义的加载类的逻辑, 我们优先使用当前的ClassLoader去完成要去进行热加载的类,
     * 对于不用热加载的类, 我们直接交给parentClassLoader去进行加载
     *
     * @param name className
     * @param resolve 是否要完成链接?
     * @return 根据className加载到的类
     */
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        synchronized(getClassLoadingLock(name)) {
            // 1. 尝试去检查缓存, 如果缓存当中已经有了, 就不必去进行类加载了
            var loadedClass = findLoadedClass(name)

            // 2. 如果缓存当中没有, 那么我们就得尝试去进行加载了
            if (loadedClass == null) {
                // 2.1 尝试去当前ClassLoader去进行加载, 看是否能加载到?
                loadedClass = try {
                    findClass(name)
                } catch (ex: ClassNotFoundException) {
                    // 2.2 如果当前ClassLoader加载不到, 那么尝试交给parent去进行加载
                    Class.forName(name, false, parent)
                }
            }
            // 如果要去完成初始化, 需要去对该类去完成链接工作(Linkage)
            if (resolve) {
                resolveClass(loadedClass)
            }
            return loadedClass
        }
    }

    /**
     * 重写findClass的逻辑, 因为我们有可能需要的类是来自于ClassLoaderFiles的,
     * 因此, 我们优先从ClassLoaderFiles当中去获取文件, 如果实在获取不到,
     * 再交给parentClassLoader去尝试进行加载
     *
     * @param name className
     * @return 根据className去加载到的类
     */
    override fun findClass(name: String): Class<*> {
        val path = name.replace(".", "/") + ".class"

        // 检查UpdatedFiles当中是否才能在有该文件? 如果没有直接调用super
        val file = updatedFiles.getFile(path) ?: return super.findClass(name)

        // 如果该文件是被删除了, 那么丢出ClassNotFoundException
        if (file.kind == ClassLoaderFile.Kind.DELETED) {
            throw ClassNotFoundException(name)
        }

        // 如果文件不是被删除了, 那么我们直接根据传输的ByteArray去进行defineClass
        return defineClass(name, file.contents, 0, file.contents.size)
    }
}