package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 提供对于Container重复的注解的寻找的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 *
 * @param parent parent, 当在本地无法寻找到合适的重复注解时, 尝试从parent去进行寻找
 */
abstract class RepeatableContainers(@Nullable private val parent: RepeatableContainers?) {

    companion object {

        /**
         * 不去Container当中去寻找重复注解的的RepeatableContainers对象
         *
         * @return RepeatableContainers
         */
        @JvmStatic
        fun none(): RepeatableContainers = NoRepeatableContainers

        /**
         * 标准的寻找Repeatable注解的RepeatableContainers
         *
         * @return RepeatableContainers
         */
        @JvmStatic
        fun standardRepeatables(): RepeatableContainers = StandardRepeatableContainers
    }

    /**
     * 为指定的注解, 去找到它的重复注解
     *
     * @param annotation annotation
     */
    @Nullable
    open fun findRepeatedAnnotations(annotation: Annotation?): Array<Annotation>? {
        annotation ?: return null
        return this.parent?.findRepeatedAnnotations(annotation)
    }

    private object StandardRepeatableContainers : RepeatableContainers(null) {

        /**
         * Cache, Key-注解类型, Value-该注解当中的存放重复注解的Container方法(不存在的话, 值为[NONE])
         */
        @JvmStatic
        private val cache = ConcurrentHashMap<Class<out Annotation>, Any>()

        /**
         * 在某个注解当中, 没有找到合适的重复注解的Container的常量标识
         */
        @JvmStatic
        private val NONE = Any()

        /**
         * 寻找重复的注解
         *
         * @param annotation 目标Container注解(比如@PropertySource, 会被合成成为一个@PropertySources的Container注解当中)
         * @return 从Container当中去寻找到的重复注解(如果找不到的话return null)
         */
        @Suppress("UNCHECKED_CAST")
        @Nullable
        override fun findRepeatedAnnotations(@Nullable annotation: Annotation?): Array<Annotation>? {
            annotation ?: return null
            val method = getRepeatedAnnotationsMethod(annotation.annotationClass.java)
            if (method != null) {
                return ReflectionUtils.invokeMethod(method, annotation) as Array<Annotation>?
            }

            // 如果没有找到的话, 那么委托parent去进行寻找
            return super.findRepeatedAnnotations(annotation)
        }

        /**
         * 获取到给定AnnotationType当中的存放重复注解的属性方法
         *
         * @param annotationType 要去进行寻找重复注解的目标Container注解(比如@PropertySources)
         * @return 如果在该注解上找到了合适的存放重复注解的属性方法, 那么return 该属性方法; 否则return null
         */
        @JvmStatic
        @Nullable
        private fun getRepeatedAnnotationsMethod(annotationType: Class<out Annotation>): Method? {
            val method = cache.computeIfAbsent(annotationType, this::computeRepeatedAnnotationsMethod)
            return if (method === NONE) return null else method as Method
        }

        /**
         * 计算出来给定的AnnotationType对应的存放重复注解的属性方法
         *
         * @param annotationType 要去进行寻找重复注解的目标Container注解(比如@PropertySources)
         * @return 如果找到了合适的存放重复注解的属性方法的话, return 该属性方法; 否则return [NONE]
         */
        @JvmStatic
        private fun computeRepeatedAnnotationsMethod(annotationType: Class<out Annotation>): Any {
            val attributeMethods = AttributeMethods.forAnnotationType(annotationType)
            if (attributeMethods.size > 0) {
                val method = attributeMethods[0]
                val returnType = method.returnType
                if (returnType.isArray) {
                    val componentType = returnType.componentType
                    if (ClassUtils.isAssignFrom(
                            Annotation::class.java, componentType
                        ) && componentType.isAnnotationPresent(
                            annotationType
                        )
                    ) {
                        return method
                    }
                }
            }
            return NONE
        }
    }


    /**
     * 不去Container当中去寻找重复注解的的RepeatableContainers对象
     */
    private object NoRepeatableContainers : RepeatableContainers(null)
}