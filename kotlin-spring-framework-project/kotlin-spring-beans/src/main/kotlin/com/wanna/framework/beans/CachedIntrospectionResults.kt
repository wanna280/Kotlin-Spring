package com.wanna.framework.beans

import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.lang.Nullable
import com.wanna.common.logging.LoggerFactory
import java.beans.BeanInfo
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/13
 */
class CachedIntrospectionResults private constructor(private val beanClass: Class<*>) {

    /**
     * JavaBeans BeanInfo
     */
    private val beanInfo: BeanInfo = getBeanInfo(beanClass)

    /**
     * 根据PropertyName去获取到JavaBeans PropertyDescriptor的缓存
     */
    private val propertyDescriptors = LinkedHashMap<String, PropertyDescriptor>()

    /**
     * TypeDescriptor Cache
     */
    private val typeDescriptorCache = ConcurrentHashMap<PropertyDescriptor, TypeDescriptor>()

    init {
        if (logger.isTraceEnabled) {
            logger.trace("Getting BeanInfo for class [${beanClass.name}]")
        }
        if (logger.isTraceEnabled) {
            logger.trace("Caching PropertyDescriptors for class [${beanClass.name}]")
        }

        val pds = this.beanInfo.propertyDescriptors
        for (pd in pds) {
            propertyDescriptors[pd.name] = buildGenericTypeAwarePropertyDescriptor(beanClass, pd)
        }

        // TODO 处理接口
    }

    /**
     * 构建一个支持去进行泛型的处理的[PropertyDescriptor]
     *
     * @param beanClass beanClass
     * @param pd PropertyDescriptor
     */
    private fun buildGenericTypeAwarePropertyDescriptor(
        beanClass: Class<*>,
        pd: PropertyDescriptor
    ): PropertyDescriptor {

        // TODO
        return pd
    }

    /**
     * 获取到所有的属性的[PropertyDescriptor]
     *
     * @return PropertyDescriptors
     */
    fun getPropertyDescriptors(): Array<PropertyDescriptor> = this.propertyDescriptors.values.toTypedArray()

    /**
     * 根据属性名去获取到对应的[PropertyDescriptor]
     *
     * @param name 属性名
     * @return 该属性名对应的[PropertyDescriptor], 获取不到return null
     */
    @Nullable
    fun getPropertyDescriptor(name: String): PropertyDescriptor? = this.propertyDescriptors[name]

    /**
     * 添加一个[TypeDescriptor]
     *
     * @param pd PropertyDescriptor
     * @param td 需要去进行添加的TypeDescriptor
     * @return 如果之前Cache当中已经存在有[TypeDescriptor]的话, 那么返回已经存在的, 否则返回给定的TypeDescriptor
     */
    fun addTypeDescriptor(pd: PropertyDescriptor, td: TypeDescriptor): TypeDescriptor =
        this.typeDescriptorCache.putIfAbsent(pd, td) ?: td

    /**
     * 根据[PropertyDescriptor]去获取到对应的[TypeDescriptor]
     *
     * @param pd PropertyDescriptor
     * @return TypeDescriptor(or null)
     */
    @Nullable
    fun getTypeDescriptor(pd: PropertyDescriptor): TypeDescriptor? = this.typeDescriptorCache[pd]

    companion object {
        /**
         * 空的[PropertyDescriptor]数组
         */
        @JvmStatic
        private val EMPTY_PROPERTY_DESCRIPTOR_ARRAY = emptyArray<Array<PropertyDescriptor>>()

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CachedIntrospectionResults::class.java)

        /**
         * 强引用的Cache, 通过Class去缓存当中找到很适合的[CachedIntrospectionResults]对象
         */
        @JvmStatic
        private val strongClassCache = ConcurrentHashMap<Class<*>, CachedIntrospectionResults>()

        /**
         * 为给定的beanClass去进行构建[CachedIntrospectionResults]的工厂方法
         *
         * @param beanClass beanClass
         * @return CachedIntrospectionResults for given beanClass
         */
        @JvmStatic
        fun forClass(beanClass: Class<*>): CachedIntrospectionResults {
            var results = strongClassCache[beanClass]
            if (results != null) {
                return results
            }
            // TODO, 这个缓存, 其实应该新加一个CacheSafe的判断&新加一个若引用的缓存

            results = CachedIntrospectionResults(beanClass)
            val exists = strongClassCache.putIfAbsent(beanClass, results)
            return exists ?: results
        }

        @JvmStatic
        private fun getBeanInfo(beanClass: Class<*>): BeanInfo {
            return Introspector.getBeanInfo(beanClass)
        }
    }

}