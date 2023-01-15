package com.wanna.framework.core.io.support

import com.wanna.framework.core.io.Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.io.IOException
import java.net.UnknownHostException
import java.util.*

/**
 * 为PropertiesLoader提供支持的基础类, 这个类当中主要提供mergeProperties方法,
 * 实现对于LocalProperties和Resource当中的Properties的Merge功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/22
 * @see mergeProperties
 */
abstract class PropertiesLoaderSupport {

    /**
     * Logger
     */
    protected val logger: Logger = LoggerFactory.getLogger(PropertiesLoaderSupport::class.java)

    /**
     * 本地的Properties
     */
    protected var localProperties: Array<Properties>? = null

    /**
     * 资源位置
     */
    protected var locations: Array<Resource>? = null

    /**
     * 是否需要忽略掉资源找不到的情况？
     */
    var ignoreResourceNotFound = false

    /**
     * 是否LocalProperties需要去覆盖从文件当中加载到的Properties？
     * 如果为true, 先加载文件、再加载Properties; 如果为false, 先加载Properties再加载文件
     *
     * @see mergeProperties
     */
    var localOverride = false

    /**
     * 设置Properties, 填充localProperties属性
     *
     * @param properties properties
     */
    open fun setProperties(properties: Properties) {
        this.localProperties = arrayOf(properties)
    }

    /**
     * 设置Properties, 填充localProperties属性
     *
     * @param propertiesArray 需要去进行填充的Properties数组
     */
    open fun setPropertiesArray(vararg propertiesArray: Properties) {
        this.localProperties = arrayOf(*propertiesArray)
    }

    /**
     * 设置资源路径, 用于最终进行资源的加载
     *
     * @param location 资源路径
     * @see mergeProperties
     */
    open fun setLocation(location: Resource) {
        this.locations = arrayOf(location)
    }

    /**
     * 设置资源路径, 用于最终进行资源的加载
     *
     * @param locations 资源路径列表
     * @see mergeProperties
     */
    open fun setLocationArray(vararg locations: Resource) {
        this.locations = arrayOf(*locations)
    }

    /**
     * Merge所有的Properties(资源文件和Properties), 得到一个大的Properties
     *
     * @return Merge之后的Properties
     */
    protected open fun mergeProperties(): Properties {
        val properties = Properties()

        // 如果localOverride=true, 说明需要使用localProperties的属性去替换资源文件的属性
        if (this.localOverride) {
            loadProperties(properties)
        }

        // 把LocalProperties当中的属性去Merge到Properties当中
        localProperties?.forEach(properties::putAll)

        // 如果localOverride=false, 说明需要使用资源文件去替换掉localProperties当中的属性
        if (!this.localOverride) {
            loadProperties(properties)
        }
        return properties
    }

    /**
     * 加载locations当中的资源, 填充到Properties当中
     *
     * @param properties 需要填充属性的Properties
     * @see setLocationArray
     */
    protected open fun loadProperties(properties: Properties) {
        this.locations?.forEach {
            if (logger.isDebugEnabled) {
                logger.debug("正在从[$it]当中去加载Properties文件")
            }
            try {
                PropertiesLoaderUtils.fillProperties(properties, it)
            } catch (ex: IOException) {
                // 如果是资源找不到, 但是确实是需要忽略的话, 那么我们直接pass掉
                if (ignoreResourceNotFound && (ex is FileNotFoundException || ex is UnknownHostException)) {
                    if (logger.isInfoEnabled) {
                        logger.info("无法找到指定的资源文件[$it]")
                    }
                } else {
                    throw ex
                }
            }
        }
    }

}