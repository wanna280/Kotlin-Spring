package com.wanna.framework.core.io

import com.wanna.framework.core.io.ResourceLoader.Companion.CLASSPATH_URL_PREFIX
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ResourceUtils
import java.net.MalformedURLException
import java.net.URL

/**
 * Spring的资源加载器的默认实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 *
 * @param classLoader 进行资源加载时用到的ClassLoader(可以为空，默认实现为)
 */
open class DefaultResourceLoader(@Nullable private var classLoader: ClassLoader? = ClassUtils.getDefaultClassLoader()) :
    ResourceLoader {

    /**
     * ProtocolResolver列表，是一个策略接口，用于去提供资源的解析
     */
    private val protocolResolvers = LinkedHashSet<ProtocolResolver>()

    /**
     * 添加一个自定义的ProtocolResolver，用于去自定义资源的加载；
     * 比如你想定义一个"wanna:/xxx/xxx"这样的路径，就可以去自定义一个ProtocolResolver
     *
     * @param protocolResolver 你想要添加的ProtocolResolver
     */
    open fun addProtocolResolver(protocolResolver: ProtocolResolver) {
        this.protocolResolvers += protocolResolver
    }

    /**
     * 根据给定的资源的位置，去加载Resource
     *
     * @param location location
     * @return Resource
     */
    override fun getResource(location: String): Resource {
        // 遍历所有的ProtocolResolver，尝试去进行资源的加载
        // 直到找到一个可以去进行资源的解析的ProtocolResolver去解析出来Resource
        protocolResolvers.forEach {
            val resource = it.resolve(location, this)
            if (resource != null) {
                return resource
            }
        }
        // 如果是以"classpath:"开头的location，那么需要把"classpath:"切掉并构建成为ClassPathResource
        return if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length), classLoader)
        } else if (location.startsWith("/")) {
            getResourceByPath(location)
        } else {
            try {
                val url = URL(location)
                if (ResourceUtils.isFileURL(url)) FileUrlResource(url) else UrlResource(url)
            } catch (ex: MalformedURLException) {
                getResourceByPath(location)
            }
        }
    }

    /**
     * 根据给定的path，去加载到合适的资源
     *
     * @param path 资源路径path
     * @return 根据path去加载到的资源
     */
    protected open fun getResourceByPath(path: String): Resource {
        return ClassPathContextResource(path, classLoader)
    }

    /**
     * 设置用于进行资源加载的ClassLoader
     *
     * @param classLoader classLoader
     */
    open fun setClassLoader(@Nullable classLoader: ClassLoader?) {
        this.classLoader = classLoader
    }

    override fun getClassLoader() = this.classLoader

    protected open inner class ClassPathContextResource(path: String, classLoader: ClassLoader?) :
        ClassPathResource(path, classLoader), ContextResource {
        override fun getPathWithinContext() = getPath()

        override fun createRelative(relativePath: String): Resource {
            val pathToUse = ResourceUtils.applyRelativePath(getPath(), relativePath)
            return ClassPathContextResource(pathToUse, getClassLoader())
        }
    }
}