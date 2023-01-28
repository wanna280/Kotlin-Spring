package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 提供了基于顺序(index)的一种快速访问注解的属性方法的方式(quick way),
 * 同时也提供了很多关于操作注解属性的相关的工具方法
 *
 * 因为注解本质上其实是一个接口, 注解的相关的属性本质上是一个接口方法调用,
 * 注解的真正的方法执行, 是使用的JDK动态代理的方式去生成的代理, 保证我们可以
 * 去正常访问注解当中去进行配置的属性信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 *
 * @param annotationType 要封装的是哪个注解的方法?
 * @param attributeMethods 注解内部的属性方法
 */
class AttributeMethods(
    @Nullable private val annotationType: Class<out Annotation>?, private val attributeMethods: Array<Method>
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
        @JvmStatic
        private val NONE = AttributeMethods(null, emptyArray())

        /**
         * AnnotationType-->AttributesMethod缓存
         */
        @JvmStatic
        private val cache = ConcurrentHashMap<Class<out Annotation>, AttributeMethods>()

        /**
         * 对目标属性方法去进行描述
         *
         * @param method 要去进行描述的目标属性方法
         * @return 描述信息
         */
        @JvmStatic
        fun describe(method: Method): String {
            return method.toGenericString()
        }

        /**
         * 对目标注解上的目标属性去进行描述
         *
         * @param annotationType 目标注解
         * @param attributeName 目标注解的属性名
         * @return 描述信息
         */
        @JvmStatic
        fun describe(annotationType: Class<out Annotation>, attributeName: String): String {
            return annotationType.name + "." + attributeName
        }

        /**
         * 提供一个基于给定一个注解, 去构建出来的AttributeMethods对象的工厂方法
         *
         * @param annotationType 想要为哪个注解去构建AttributeMethods?
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
         * @param annotationType 想要为哪个注解去构建AttributeMethods?
         * @return 构建出来的AttributesMethods
         */
        @JvmStatic
        private fun compute(annotationType: Class<out Annotation>): AttributeMethods {
            // Note: 这里要使用DeclaredMethods, 不能使用Methods, 否则hashCode/toString等方法都会涉及到, 会有问题
            val attributeMethods = annotationType.declaredMethods
                .filter { isAttributeMethod(it) }.sortedWith(methodComparator)
                .toTypedArray()
            return if (attributeMethods.isEmpty()) NONE else AttributeMethods(annotationType, attributeMethods)
        }

        /**
         * 判断给定的方法是否是一个注解的属性方法?
         *
         * @param method 待进行判断的方法
         * @return 如果它是一个属性方法, 那么return true; 否则return false
         */
        @JvmStatic
        private fun isAttributeMethod(method: Method): Boolean =
            method.parameterCount == 0 && method.returnType != Unit::class.java
    }

    /**
     * 对注解去进行检验, 检验该注解当中定义的属性, 是否都是合法的?
     * 因为很多时候, 有可能会遇到, 一个类上标注了A注解, 但是A注解的属性当中, 用到了一个类B,
     * 但是类B很可能并不在我们的依赖当中, 此时就会产生[ClassNotFoundException]/[LinkageError],
     * 但是很多时候, 我们允许去进行这样的配置, 我们很可能会继续尝试使用ASM去进行该注解当中的属性的读取
     *
     * @param annotation 待检验的注解对象
     */
    fun validate(annotation: Annotation) {
        for (i in 0 until size) {
            try {
                AnnotationUtils.invokeAnnotationMethod(attributeMethods[i], annotation)
            } catch (ex: Throwable) {
                throw IllegalStateException("Could not obtain annotation attribute value for '${get(i).name}' declared on '$annotationType'")
            }
        }
    }

    /**
     * 从AttributeMethods当中根据name去找到合适的属性对应的index
     *
     * @param name 属性名
     * @return 如果存在有这样的属性, 那么返回该属性所在的index; 如果不存在, 那么return -1
     */
    fun indexOf(name: String): Int {
        attributeMethods.indices.forEach {
            if (attributeMethods[it].name == name) {
                return it
            }
        }
        return -1
    }

    /**
     * 从AttributeMethods当中, 去定位到给定的方法所在的index
     *
     * @param attribute attributeMethod
     * @return 该方法所在的位置index, 不存在的话return -1
     */
    fun indexOf(attribute: Method): Int {
        attributeMethods.indices.forEach {
            if (attributeMethods[it] == attribute) {
                return it
            }
        }
        return -1
    }

    /**
     * 从AttributeMethods当中, 根据name去定位到对应的属性方法
     *
     * @param name name
     * @return 根据name获取到的属性方法(如果不存在的话, return null)
     */
    @Nullable
    fun get(name: String): Method? {
        val index = indexOf(name)
        return if (index == -1) null else this[index]
    }

    /**
     * 根据index去获取到对应的属性方法
     *
     * @param index index
     * @return 该位置的属性方法
     */
    operator fun get(index: Int): Method = attributeMethods[index]

    /**
     * 属性方法的数量
     */
    val size: Int
        get() = this.attributeMethods.size

}