package com.wanna.framework.core.util

import com.wanna.framework.core.DefaultParameterNameDiscoverer
import java.beans.ConstructorProperties
import java.lang.reflect.Constructor

object BeanUtils {

    // 参数名发现器，提供方法/构造器当中的参数名的获取
    private val parameterNameDiscoverer = DefaultParameterNameDiscoverer()

    // Key-包装类型，Value-基础类型
    private val primitiveWrapperTypeMap = LinkedHashMap<Class<*>, Class<*>>(8)

    // Key-基础类型，Value-包装类型
    private val primitiveTypeToWrapperMap = LinkedHashMap<Class<*>, Class<*>>(8)

    init {
        primitiveWrapperTypeMap[Int::class.javaObjectType] = Int::class.java
        primitiveWrapperTypeMap[Double::class.javaObjectType] = Double::class.java
        primitiveWrapperTypeMap[Short::class.javaObjectType] = Short::class.java
        primitiveWrapperTypeMap[Byte::class.javaObjectType] = Byte::class.java
        primitiveWrapperTypeMap[Long::class.javaObjectType] = Long::class.java
        primitiveWrapperTypeMap[Float::class.javaObjectType] = Float::class.java
        primitiveWrapperTypeMap[Char::class.javaObjectType] = Char::class.java
        primitiveWrapperTypeMap[Boolean::class.javaObjectType] = Boolean::class.java

        primitiveWrapperTypeMap.forEach { (k, v) -> primitiveTypeToWrapperMap[v] = k }
    }

    /**
     * 通过有参数构造器去创建对象
     * @param ctor 构造器
     * @param args 参数列表
     */
    @JvmStatic
    fun <T> instantiateClass(ctor: Constructor<T>, vararg args: Any?): T {
        return ctor.newInstance(*args)
    }

    /**
     * 通过无参数构造器去创建对象
     * @param ctor 无参构造器
     */
    @JvmStatic
    fun <T> instantiateClass(ctor: Constructor<T>): T {
        return ctor.newInstance()
    }

    /**
     * 通过无参数构造器创建对象
     */
    @JvmStatic
    fun <T> instantiateClass(clazz: Class<T>): T {
        return clazz.getDeclaredConstructor().newInstance()
    }

    /**
     * 获取一个构造器的参数名列表；
     * 如果存在有JDK当中提供的@ConstructorProperties注解，那么从它上面去找；
     * 如果没有@ConstructorProperties注解，那么使用DefaultParameterNameDiscoverer去进行寻找
     *
     * @param ctor 要获取参数名的目标构造器
     * @throws IllegalStateException 如果没有找到合适的参数名列表/找到的参数名列表长度不对
     */
    @JvmStatic
    fun getParameterNames(ctor: Constructor<*>): Array<String> {
        val cp = ctor.getAnnotation(ConstructorProperties::class.java)
        val parameterNames: Array<String> = cp?.value ?: parameterNameDiscoverer.getParameterNames(ctor)
        ?: throw IllegalStateException("无法从目标构造器[ctor=$ctor]上获取到参数名列表")
        if (parameterNames.size != ctor.parameterCount) {
            throw IllegalStateException("匹配到的参数名的数量和目标构造器的参数数量不相同")
        }
        return parameterNames
    }

    /**
     * 判断它是否是一个简单类型
     */
    @JvmStatic
    fun isSimpleProperty(type: Class<*>): Boolean {
        if (isPrimitive(type) || isPrimitiveWrapper(type)) {
            return true
        }
        if (type == Class::class.java || ClassUtils.isAssignFrom(
                CharSequence::class.java, type
            ) || ClassUtils.isAssignFrom(Number::class.java, type)
        ) {
            return true
        }
        return false
    }

    /**
     * 判断它是否是一个基础数据类型的Wrapper(Integer/Double等)
     *
     * @param clazz 要进行判断的类型
     * @return 如果它是一个包装类型return true；否则return false
     */
    @JvmStatic
    fun isPrimitiveWrapper(clazz: Class<*>): Boolean {
        return primitiveWrapperTypeMap.containsKey(clazz)
    }

    /**
     * 判断它是否是一个基础数据类型(int/double等)
     *
     * @param clazz 要进行判断的类型
     * @return 如果它是一个基础数据类型return true；否则return false
     */
    @JvmStatic
    fun isPrimitive(clazz: Class<*>): Boolean {
        return primitiveTypeToWrapperMap.containsKey(clazz)
    }
}