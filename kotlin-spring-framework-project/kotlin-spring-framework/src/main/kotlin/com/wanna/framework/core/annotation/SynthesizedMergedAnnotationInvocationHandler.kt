package com.wanna.framework.core.annotation

import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.Arrays
import java.util.concurrent.ConcurrentHashMap

/**
 * 合成的用于生成注解的InvocationHandler, 在Spring当中需要用到自定义的注解的合成,
 * 合成注解时, 需要使用到JDK的动态代理, 从而最终使用到InvocationHandler
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 * @see InvocationHandler
 *
 * @param annotation 合成的注解MergedAnnotation对象
 * @param type 原始的注解的类型
 * @param A  原始注解的类型
 */
class SynthesizedMergedAnnotationInvocationHandler<A : Annotation>
    (private val annotation: MergedAnnotation<A>, private val type: Class<A>) : InvocationHandler {

    /**
     * 为给定的注解类型去构建出来该注解的属性方法
     */
    private val attributes = AttributeMethods.forAnnotationType(type)

    /**
     * 属性值的缓存, 避免属性值的重复计算, 将属性值存起来
     */
    private val valueCache = ConcurrentHashMap<String, Any>()

    /**
     * 缓存一个注解的hashCode之后的结果
     */
    @Nullable
    @Volatile
    private var hashCode: Int? = null

    /**
     * 缓存注解的toString之后的结果
     */
    @Nullable
    @Volatile
    private var string: String? = null

    /**
     * 拦截注解对象的一些目标方法, 是JDK动态代理需要去进行执行的回调方法
     *
     * @param proxy proxy代理对象
     * @param method 拦截到的目标方法
     * @param args 执行目标方法需要用到的参数列表
     */
    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        // 1.如果是equals方法
        if (ReflectionUtils.isEqualsMethod(method)) {
            return annotationEquals(proxy)
        }
        // 2.如果是HashCode方法
        if (ReflectionUtils.isHashCodeMethod(method)) {
            return annotationHashCode()
        }
        // 3.如果是toString方法
        if (ReflectionUtils.isToStringMethod(method)) {
            return annotationToString()
        }
        // 4.如果是annotationType方法
        if (isAnnotationTypeMethod(method)) {
            return this.type
        }
        // 5.如果是注解属性方法, 那么通过MergedAnnotation去获取到对应的属性值
        if (attributes.indexOf(method.name) != -1) {
            return getAttributeValue(method)
        }
        // 丢出AnnotationConfigException
        throw AnnotationConfigurationException(
            String.format("Method [%s] is unsupported for synthesized annotation type [%s]", method, type)
        )
    }

    /**
     * 注解的"equals"比较
     *
     * @param other 别的注解对象
     * @return 如果类型&属性值完全相同计算equals, return true; 否则return false
     */
    private fun annotationEquals(other: Any?): Boolean {
        other ?: return false
        // 1.先检查引用
        if (this == other) {
            return true
        }
        // 2.检查类型
        if (!this.type.isInstance(other)) {
            return false
        }
        // 3.对所有的属性去进行equals检查...
        for (i in 0 until attributes.size) {
            val attribute = attributes[i]
            val attributeValue = getAttributeValue(attribute)
            val otherAttributeValue = ReflectionUtils.invokeMethod(attribute, other)
            if (attributeValue != otherAttributeValue) {
                return false
            }
        }
        return true
    }


    /**
     * 计算一个直接到hashCode的结果
     *
     * @return hashCode
     */
    private fun annotationHashCode(): Int {
        var hashCode = this.hashCode
        if (hashCode == null) {
            hashCode = computeHashCode()
            this.hashCode = hashCode
        }
        return hashCode
    }

    /**
     * 计算一个注解的toString的结果
     *
     * @return toString
     */
    private fun annotationToString(): String {
        var string = this.string
        if (string == null) {
            // @type, 其实也就是注解的表示方式
            val builder = StringBuilder("@").append(type.name).append("(")
            for (i in 0 until attributes.size) {
                if (i > 0) {
                    builder.append(", ")
                }
                val attribute = attributes[i]
                builder.append(attribute.name).append("=").append(getAttributeValue(attribute))
            }
            builder.append(")")
            string = builder.toString()
            this.string = string
        }
        return string
    }

    /**
     * 将给定的对象, toString
     */
    private fun toString(value: Any): String {
        if (value is Class<*>) {
            return value.name
        }
        if (value.javaClass.isArray) {
            val builder = StringBuilder("[")
            for (i in 0 until java.lang.reflect.Array.getLength(value)) {
                if (i > 0) {
                    builder.append(", ")
                }
                // 递归toString
                builder.append(toString(java.lang.reflect.Array.get(value, i)))
            }
            return builder.toString()
        }
        return value.toString()
    }

    /**
     * 检查这个方法是否是注解的AnnotationType方法
     *
     * @param method method
     * @return 如果它是AnnotationType方法return true; 否则return false
     */
    private fun isAnnotationTypeMethod(method: Method): Boolean {
        return method.name == "annotationType" && method.parameterCount == 0
    }

    /**
     * 根据注解的属性方法, 去获取到对应的属性值
     *
     * @param method 注解的属性方法
     * @return 注解当中该属性方法对应的属性值
     */
    private fun getAttributeValue(method: Method): Any {
        // 将注解的属性值计算之后放入到valueCache当中, 下次直接从缓存当中去进行获取
        val value = this.valueCache.computeIfAbsent(method.name) { attributeName ->
            val attributeType = ClassUtils.resolvePrimitiveIfNecessary(method.returnType)
            return@computeIfAbsent annotation.getValue(attributeName, attributeType).orElseThrow {
                NoSuchElementException("No value found for attribute named '${attributeName}' in merged annotation ${type.name}")
            }
        }
        // 如果返回的是一个数组的话, 那么clone一份去进行返回, 别让使用方修改这个缓存当中的内容
        if (value.javaClass.isArray && java.lang.reflect.Array.getLength(value) == 0) {
            return cloneArray(value)
        }
        return value
    }

    /**
     * 计算当前注解的hashCode
     *
     * @return hashCode
     */
    private fun computeHashCode(): Int {
        var hashCode = 0
        for (i in 0 until attributes.size) {
            val attribute = attributes[i]
            val value = getAttributeValue(attribute)
            hashCode += (127 * attribute.name.hashCode()) xor getValueHashCode(value)
        }
        return hashCode
    }

    /**
     * 计算单个属性值的hashCode
     *
     * @param value 要去进行计算hashCode的属性值
     * @return hashCode
     */
    private fun getValueHashCode(value: Any): Int {
        if (value is BooleanArray) {
            return value.contentHashCode()
        }
        if (value is ByteArray) {
            return value.contentHashCode()
        }
        if (value is CharArray) {
            return value.contentHashCode()
        }
        if (value is DoubleArray) {
            return value.contentHashCode()
        }
        if (value is FloatArray) {
            return value.contentHashCode()
        }
        if (value is ShortArray) {
            return value.contentHashCode()
        }
        if (value is IntArray) {
            return value.contentHashCode()
        }
        if (value is LongArray) {
            return value.contentHashCode()
        }
        if (value is Array<*>) {
            return value.contentHashCode()
        }
        return value.hashCode()
    }

    /**
     * 执行对于数组的克隆
     *
     * @param array 原始的数组array
     * @return Clone之后的数组对象
     */
    private fun cloneArray(array: Any): Any {
        if (array is BooleanArray) {
            return array.clone()
        }
        if (array is ByteArray) {
            return array.clone()
        }
        if (array is CharArray) {
            return array.clone()
        }
        if (array is DoubleArray) {
            return array.clone()
        }
        if (array is FloatArray) {
            return array.clone()
        }
        if (array is ShortArray) {
            return array.clone()
        }
        if (array is IntArray) {
            return array.clone()
        }
        if (array is LongArray) {
            return array.clone()
        }
        return (array as Array<*>).clone()
    }

    companion object {
        /**
         * 为给定的注解对象去创建代理对象, 用来去合成一个代理出来的注解对象
         *
         * @param annotation MergedAnnotation
         * @param type annotationType
         * @param A annotationType
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <A : Annotation> createProxy(annotation: MergedAnnotation<A>, type: Class<A>): A {
            // 如果SynthesizedAnnotation对当前ClassLoader可见, 那么给代理接口添加上SynthesizedAnnotation作为标识接口
            val proxyInterfaces = if (isVisible(type.classLoader, SynthesizedAnnotation::class.java)) arrayOf(
                type, SynthesizedAnnotation::class.java
            ) else arrayOf(type)

            val handler = SynthesizedMergedAnnotationInvocationHandler(annotation, type)

            // 利用JDK动态代理, 去生成一个注解实例对象
            return Proxy.newProxyInstance(type.classLoader, proxyInterfaces, handler) as A
        }

        /**
         * 检查给定的clazz是否对ClassLoader可见?
         *
         * @param classLoader ClassLoader
         * @param interfaceClass interfaceClass
         */
        @JvmStatic
        private fun isVisible(classLoader: ClassLoader, interfaceClass: Class<*>): Boolean {
            if (classLoader == interfaceClass.classLoader) {
                return true
            }
            try {
                return Class.forName(interfaceClass.name, false, classLoader) == interfaceClass
            } catch (ex: Throwable) {
                return false
            }
        }
    }
}