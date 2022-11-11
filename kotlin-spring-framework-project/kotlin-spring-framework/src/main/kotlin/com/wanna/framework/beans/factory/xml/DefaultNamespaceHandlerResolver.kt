package com.wanna.framework.beans.factory.xml

import com.wanna.framework.core.io.support.PropertiesLoaderUtils
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils
import java.util.concurrent.ConcurrentHashMap

/**
 * 默认的NamespaceHandler的解析器的实现，从"META-INF/spring.handlers"当中去加载所有的配置文件；
 * 并提供对外获取NamespaceHandler的解析的相关功能，外部传入一个NamespaceId，需要去解析该NamespaceId
 * 对应的NamespaceHandler，方便完成后续的配置文件的解析工作
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 *
 * @param classLoader 加载资源的ClassLoader
 * @param handlerMappingsLocation NamespaceHandler的资源路径
 */
open class DefaultNamespaceHandlerResolver(
    classLoader: ClassLoader?,
    private val handlerMappingsLocation: String
) : NamespaceHandlerResolver {
    companion object {
        /**
         * 默认的NamespaceHandler所在的位置
         */
        const val DEFAULT_HANDLER_MAPPINGS_LOCATION = "META-INF/spring.handlers"
    }

    /**
     * ClassLoader，有默认值
     */
    private val classLoader: ClassLoader = classLoader ?: ClassUtils.getDefaultClassLoader()

    /**
     * NamespaceHandler的结果，如果该NamespaceHandler被获取过，那么Value就是该实例对象；
     * 如果没获取过，那么就是从"META-INF/spring.handlers"当中加载到的属性值
     */
    @Volatile
    private var handlerMappings: MutableMap<String, Any>? = null

    /**
     * 提供一个只给定ClassLoader的构造器
     *
     * @param classLoader ClassLoader
     */
    constructor(classLoader: ClassLoader?) : this(classLoader, DEFAULT_HANDLER_MAPPINGS_LOCATION)

    /**
     * 提供一个无参数构造器
     */
    constructor() : this(null)

    /**
     * 根据NamespaceUri去解析出来对应的NamespaceHandler
     *
     * @param namespaceUri namespaceUri
     * @return 解析到的NamespaceHandler，如果不存在该namespaceUri对应的NamespaceHandler的话，return null
     */
    override fun resolve(namespaceUri: String): NamespaceHandler? {
        val handlerMappings = getHandlerMappings()
        val handlerOrClassName = handlerMappings[namespaceUri] ?: return null
        return if (handlerOrClassName is NamespaceHandler) {
            handlerOrClassName
        } else {
            val handlerClassName = handlerOrClassName.toString()
            val handlerClass = ClassUtils.forName<Any>(handlerClassName)
            if (!ClassUtils.isAssignFrom(NamespaceHandler::class.java, handlerClass)) {
                throw IllegalStateException("namespaceUri=[$namespaceUri]配置的handlerClassName[$handlerClassName]不是一个NamespaceHander")
            }
            val handler = BeanUtils.instantiateClass(handlerClass)
            handlerMappings[namespaceUri] = handler
            (handler as NamespaceHandler).init()
            handler
        }
    }

    /**
     * 获取HandlerMappings，如果之前还没初始化的话，那么使用DCL的方式去进行初始化
     *
     * @return HandlerMappings
     */
    @Suppress("UNCHECKED_CAST")
    private fun getHandlerMappings(): MutableMap<String, Any> {
        var handlerMappings = this.handlerMappings
        if (handlerMappings == null) {
            synchronized(this) {
                handlerMappings = this.handlerMappings
                if (handlerMappings == null) {
                    val properties =
                        PropertiesLoaderUtils.loadAllProperties(handlerMappingsLocation, classLoader)
                    handlerMappings = ConcurrentHashMap()
                    handlerMappings!!.putAll(properties as Map<out String, Any>)
                    this.handlerMappings = handlerMappings
                }
            }
        }
        return handlerMappings!!
    }
}