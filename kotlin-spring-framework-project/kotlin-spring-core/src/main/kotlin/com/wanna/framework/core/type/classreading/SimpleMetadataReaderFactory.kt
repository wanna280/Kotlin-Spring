package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.io.DefaultResourceLoader
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.util.ClassUtils

/**
 * 基于ASM的方式去针对给定的Class文件的读取, 从而去提供[MetadataReaderFactory]的简单实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 *
 * @param resourceLoader ResourceLoader, 提供对于资源的加载
 */
open class SimpleMetadataReaderFactory(val resourceLoader: ResourceLoader) : MetadataReaderFactory {

    /**
     * 提供一个无参数的构造器, 使用默认的ResourceLoader
     */
    constructor() : this(DefaultResourceLoader())

    /**
     * 根据className, 使用ResourceLoader加载到该ClassName对应的资源, 从而获取到
     *
     * @param className className
     * @return 读取该类的相关元信息的MetadataReader
     */
    override fun getMetadataReader(className: String): MetadataReader {
        val resourceUrl =
            ResourceLoader.CLASSPATH_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(className) + ClassUtils.CLASS_FILE_SUFFIX
        var resource = resourceLoader.getResource(resourceUrl)
        if (!resource.exists()) {
            // 有些情况下, 对于内部类, 使用的"."去分割, 例如外部类"com.wanna.UserInfo", 内部类名是"User", 那么很可能是"com.wanna.UserInfo.User"这种情况,
            // 但是Java当中是使用的"$"去进行的分割外部类和内部类, 对于ClassUtils.forName当中其实也存在有等效的检查..
            val dotIndex = className.lastIndexOf(ClassUtils.DOT)
            if (dotIndex != -1) {
                val innerClassName = className.substring(0, dotIndex) + '$' + className.substring(dotIndex + 1)
                val innerClassUrl =
                    ResourceLoader.CLASSPATH_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(innerClassName) + ClassUtils.CLASS_FILE_SUFFIX
                val innerClassResource = resourceLoader.getResource(innerClassUrl)
                if (innerClassResource.exists()) {
                    resource = innerClassResource
                }
            }
        }
        return getMetadataReader(resource)
    }

    /**
     * 根据Resource, 快速构建MetadataReader
     *
     * @param resource Resource
     * @return MetadataReader
     */
    override fun getMetadataReader(resource: Resource): MetadataReader =
        SimpleMetadataReader(resource, classLoader)

    /**
     * ClassLoader
     */
    val classLoader: ClassLoader =
        resourceLoader.getClassLoader() ?: throw IllegalStateException("Cannot get ClassLoader")

}