package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 提供了基于顺序(index)的一种快速访问注解的属性方法的方式(quick way)，
 * 同时也提供了很多关于操作注解属性的相关的工具方法
 *
 * 因为注解本质上其实是一个接口，注解的相关的属性本质上是一个接口方法调用，
 * 注解的真正的方法执行，是使用的JDK动态代理的方式去生成的代理，保证我们可以
 * 去正常访问注解当中去进行配置的属性信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 *
 * @param annotationType 要封装的是哪个注解的方法？
 * @param attributeMethods 注解内部的属性方法
 */
class AttributeMethods(
    @Nullable private val annotationType: Class<out Annotation>?,
    private val attributeMethods: Array<Method>
) {

    companion object {

        /**
         * 对方法去进行排序的比较器
         */
        @JvmStatic
        private val methodComparator = Comparator<Method> { o1, o2 -> o1.name.compareTo(o2.name) }

        /**
         * 该注解当中没有属性方法的常量
         */
        private val NONE = AttributeMethods(null, emptyArray())

        /**
         * AnnotationType-->AttributesMethod缓存
         */
        @JvmStatic
        private val cache = ConcurrentHashMap<Class<out Annotation>, AttributeMethods>()

        /**
         * 提供一个基于给定一个注解，去构建出来的AttributeMethods对象的工厂方法
         *
         * @param annotationType 想要为哪个注解去构建AttributeMethods？
         * @return 构建出来的AttributesMethods(如果给定的annotationType为null, 那么return null)
         */
        @JvmStatic
        fun forAnnotationType(@Nullable annotationType: Class<out Annotation>?): AttributeMethods {
            annotationType ?: return NONE
            return cache.computeIfAbsent(annotationType, this::compute)
        }

        /**
         * 为给定的注解类型去构建出来AttributeMethods
         *
         * @param annotationType 想要为哪个注解去构建AttributeMethods？
         * @return 构建出来的AttributesMethods
         */
        @JvmStatic
        private fun compute(annotationType: Class<out Annotation>): AttributeMethods {
            val attributeMethods =
                annotationType.methods.filter { isAttributeMethod(it) }.sortedWith(methodComparator).toTypedArray()
            return if (attributeMethods.isEmpty()) NONE else AttributeMethods(annotationType, attributeMethods)
        }

        /**
         * 判断给定的方法是否是一个注解的属性方法？
         *
         * @param method 待进行判断的方法
         * @return 如果它是一个属性方法，那么return true；否则return false
         */
        private fun isAttributeMethod(method: Method): Boolean =
            method.parameterCount == 0 && method.returnType != Unit::class.java
    }

    /**
     * 从AttributeMethods当中根据name去找到合适的属性对应的index
     *
     * @param name 属性名
     * @return 如果存在有这样的属性，那么返回该属性所在的index；如果不存在，那么return -1
     */
    fun indexOf(name: String): Int {
        attributeMethods.indices.forEach {
            if (attributeMethods[it].name == name) {
                return it
            }
        }
        return -1
    }

    fun get(index: Int): Method {
        return attributeMethods[index]
    }

    fun size(): Int {
        return this.attributeMethods.size
    }

}