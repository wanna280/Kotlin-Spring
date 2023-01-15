package com.wanna.framework.beans

import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.convert.Property
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ReflectionUtils
import java.beans.PropertyDescriptor

/**
 * BeanWrapper的具体实现，提供了属性的访问器，并组合了BeanFactory的TypeConverter，去完成Bean属性的类型的转换工作；
 *
 * 它提供了SpringBean的BeanDefinition当中的PropertyValues(pvs)当中维护的所有的属性值的设置工作，在Spring的BeanDefinition当中，
 * 通过BeanDefinition去添加PropertyValue的方式，可以去实现属性的自动注入工作；
 *
 * 当然，别的情况下，也支持去进行设置，只要你能提供目标对象以及相应的PropertyValues列表即可
 *
 * @see BeanWrapper
 * @see AbstractNestablePropertyAccessor
 */
open class BeanWrapperImpl() : BeanWrapper, AbstractNestablePropertyAccessor() {

    init {
        this.setConversionService(DefaultConversionService.getSharedInstance())
    }

    /**
     * 获取到beanType的内省的结果信息, 基于JDK原生的BeanInfo去进行实现, 并提供缓存实现
     */
    @Nullable
    private var cachedIntrospectionResults: CachedIntrospectionResults? = null

    constructor(@Nullable wrappedObject: Any?) : this() {
        this.wrappedObject = wrappedObject
    }

    constructor(obj: Any, @Nullable nestedPath: String?, @Nullable rootObject: Any?) : this() {
        this.wrappedObject = obj
        this.nestedPath = nestedPath ?: ""
        this.rootObject = rootObject
    }

    constructor(obj: Any, @Nullable nestedPath: String?, parent: AbstractNestablePropertyAccessor) : this() {
        this.wrappedObject = obj
        this.nestedPath = nestedPath ?: ""
        this.setConversionService(parent.getConversionService())
        this.autoGrowNestedPaths = parent.autoGrowNestedPaths
        this.rootObject = parent.getWrappedInstance()
    }

    /**
     * 创建一个嵌套的[AbstractNestablePropertyAccessor]
     *
     * @param instance instance
     * @param nestedPath nestedPath
     * @return 创建出来的嵌套的[AbstractNestablePropertyAccessor]实例
     */
    override fun newNestedPropertyAccessor(instance: Any, nestedPath: String): AbstractNestablePropertyAccessor {
        return BeanWrapperImpl(instance, nestedPath, this)
    }

    /**
     * 获取Bean本地的属性的PropertyHandler
     *
     * @param propertyName 需要去获取[BeanPropertyHandler]属性名
     * @return 对于给定的属性名, 去进行设置的[BeanPropertyHandler]
     */
    @Nullable
    override fun getLocalPropertyHandler(propertyName: String): PropertyHandler? {
        val pd = getCachedIntrospectionResults().getPropertyDescriptor(propertyName)
        return if (pd == null) null else BeanPropertyHandler(pd)
    }

    /**
     * 获取[CachedIntrospectionResults], 去提供对于[PropertyDescriptor]的获取的相关支持
     *
     * @return CachedIntrospectionResults
     */
    open fun getCachedIntrospectionResults(): CachedIntrospectionResults {
        if (this.cachedIntrospectionResults == null) {
            this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(getWrappedClass())
        }
        return this.cachedIntrospectionResults!!
    }

    /**
     * 设置[CachedIntrospectionResults]
     *
     * @param cachedIntrospectionResults 你想要使用的CachedIntrospectionResults
     */
    open fun setCachedIntrospectionResults(cachedIntrospectionResults: CachedIntrospectionResults) {
        this.cachedIntrospectionResults = cachedIntrospectionResults
    }

    private fun property(pd: PropertyDescriptor): Property {
        return Property(pd.javaClass, pd.readMethod, pd.writeMethod, pd.name)
    }

    /**
     * 对于Bean的属性的处理的Handler
     *
     * @param pd PropertyDescriptor, 用于去对一个Bean的属性值, 去进行描述(获取到它的Getter/Setter)
     * @param propertyType 属性值的类型
     * @param readable 是否可读? 基于Getter是否存在去进行判断
     * @param writeable 是否可写? 基于Setter是否存在去进行判断
     */
    private inner class BeanPropertyHandler(
        private val pd: PropertyDescriptor,
        propertyType: Class<*>,
        readable: Boolean,
        writeable: Boolean
    ) : PropertyHandler(propertyType, readable, writeable) {

        constructor(pd: PropertyDescriptor) : this(pd, pd.propertyType, pd.readMethod != null, pd.writeMethod != null)

        /**
         * 获取到propertyType的[ResolvableType], 使用Getter的返回值类型去进行生成
         *
         * @return propertyType的ResolvableType
         */
        override fun getResolvableType(): ResolvableType = ResolvableType.forMethodReturnType(pd.readMethod)

        /**
         * 获取到propertyType的[TypeDescriptor]
         *
         * @return propertyType的TypeDescriptor
         */
        override fun toTypeDescriptor(): TypeDescriptor = TypeDescriptor(property(pd))

        /**
         * 使用Getter的方式, 去获取到一个Bean的属性值
         *
         * @return Bean属性值(基于Getter去进行获取)
         */
        @Nullable
        override fun getValue(): Any? {
            val readMethod = this.pd.readMethod
            ReflectionUtils.makeAccessible(readMethod)
            return ReflectionUtils.invokeMethod(readMethod, getWrappedInstance())
        }

        /**
         * 使用Setter的方式, 去对一个Bean属性去进行设置
         *
         * @param value 对这个属性, 要去进行设置的值(可以为null)
         */
        override fun setValue(@Nullable value: Any?) {
            val writeMethod = this.pd.writeMethod
            ReflectionUtils.makeAccessible(writeMethod)
            ReflectionUtils.invokeMethod(writeMethod, getWrappedInstance(), value)
        }
    }

}