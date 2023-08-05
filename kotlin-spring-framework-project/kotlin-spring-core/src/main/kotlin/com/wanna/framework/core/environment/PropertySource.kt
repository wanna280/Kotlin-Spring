package com.wanna.framework.core.environment

import com.wanna.framework.lang.Nullable
import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory

/**
 * 这是一个[PropertySource], 对属性的来源进行的一层抽象;
 * Spring当中的配置文件、环境变量、系统属性等都会被抽象成为一个[PropertySource];
 * 对于一个[PropertySource], 它可以被[PropertySources]去进行聚合成为一个[PropertySource]的列表
 *
 * @see PropertySources
 * @see PropertyResolver
 * @see PropertySourcesPropertyResolver
 * @see MutablePropertySources
 *
 * @param name PropertySource name
 * @param source source, 提供属性值的获取(例如Map)
 */
@Suppress("UNCHECKED_CAST")
abstract class PropertySource<T>(var name: String, val source: T) {

    /**
     * Logger
     */
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * 从PropertySource当中去获取属性值, 抽象方法, 交给子类去实现
     *
     * @param name 属性name
     * @return 根据name获取到的属性值(如果无法获取到return null)
     */
    @Nullable
    abstract fun getProperty(name: String): Any?

    /**
     * 当前的PropertySource当中是否存在有给定的name作为属性名的属性值?
     *
     * @param name 属性名
     * @return 如果存在有这样的属性名的属性值的话, 那么return true; 否则return false
     */
    open fun containsProperty(name: String): Boolean = getProperty(name) != null

    /**
     * 对于equals的判断, 直接采用name去进行比较即可
     *
     * @param other 要去进行比较的其他对象
     * @return 两者引用相等或者name相等, return true; 否则return false
     */
    override fun equals(@Nullable other: Any?): Boolean =
        this === other || (other is PropertySource<*> && this.name == other.name)

    /**
     * hashCode, 根据name去进行生成hashCode
     *
     * @return hashCode
     */
    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String {
        // 如果是Debug模式, 那么将PropertySource详细信息去进行输出
        if (logger.isDebugEnabled) {
            return javaClass.simpleName + "@" + System.identityHashCode(this) + "(name=$name, properties=$source)"
        }
        return javaClass.simpleName + "(name=$name)"
    }
}