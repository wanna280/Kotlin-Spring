package com.wanna.framework.core.convert

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.util.StringUtils
import java.beans.PropertyDescriptor
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 对应的是JavaBeans当中的[PropertyDescriptor], 因为在很多环境下, "java.beans"这个包并不存在,
 * (例如Android/JavaME等), 因此将[PropertyDescriptor]去移植到Spring的Core包当中就是很必要的,
 * 它可以被用来去构建一个[TypeDescriptor], 对于属性值类型的转换是很有用的
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/15
 *
 * @param objectType objectType
 * @param readMethod getter
 * @param writeMethod setter
 * @param name 属性名(不给的话, 将会根据getter/setter方法去进行自动推断)
 */
class Property(
    val objectType: Class<*>,
    @Nullable val readMethod: Method?,
    @Nullable val writeMethod: Method?,
    @Nullable name: String?
) {

    companion object {
        /**
         * AnnotationCache, Key是[Property], Value是该[Property]的getter&setter/field当中的注解信息
         */
        @JvmStatic
        private val annotationCache = ConcurrentHashMap<Property, Array<Annotation>>()
    }

    /**
     * Getter的返回值/Setter的参数的参数描述信息
     */
    val methodParameter: MethodParameter = resolveMethodParameter()

    /**
     * 属性名
     */
    val name: String = name ?: resolveName()

    /**
     * 参数类型, 通过[MethodParameter]去进行计算
     */
    val type: Class<*>
        get() = methodParameter.getParameterType()

    /**
     * 该属性当中的相关的注解(包含getter&setter&field三部分的字段)
     */
    @Nullable
    val annotations: Array<Annotation>
        get() = resolveAnnotations()


    // Internal helpers, 一些内部使用的工具方法...

    /**
     * 使用Getter/Setter去解析出来[MethodParameter]
     *
     * @return MethodParameter
     */
    private fun resolveMethodParameter(): MethodParameter {
        val read = resolveReadMethodParameter()
        val write = resolveWriteMethodParameter()
        if (write == null) {
            if (read == null) {
                throw IllegalStateException("Property is neither readable nor writeable")
            }
            return read
        }
        return write
    }

    /**
     * 解析Getter的返回值去作为[MethodParameter]
     *
     * @return Getter的返回值的[MethodParameter], 如果Getter不存在return null
     */
    @Nullable
    private fun resolveReadMethodParameter(): MethodParameter? {
        return MethodParameter.forExecutable(readMethod ?: return null, -1)
    }

    /**
     * 解析Setter的第一个参数去作为[MethodParameter]
     *
     * @return Setter的第一个参数的[MethodParameter], 如果Setter不存在return null
     */
    @Nullable
    private fun resolveWriteMethodParameter(): MethodParameter? {
        return MethodParameter.forExecutable(writeMethod ?: return null, 0)
    }

    /**
     * 解析当前属性的[Annotation]注解信息, 从getter/setter/field当中同时去进行寻找
     *
     * @return 当前属性的注解列表
     */
    private fun resolveAnnotations(): Array<Annotation> {
        var annotations = annotationCache[this]
        if (annotations == null) {
            val annotationMap = LinkedHashMap<Class<*>, Annotation>()
            addAnnotationsToMap(annotationMap, readMethod)
            addAnnotationsToMap(annotationMap, writeMethod)
            addAnnotationsToMap(annotationMap, getField())
            annotations = annotationMap.values.toTypedArray()
        }
        return annotations
    }

    /**
     * 将给定的[obj]这个[AnnotatedElement]元素上的注解列表, 去添加到[annotationMap]当中
     *
     * @param annotationMap annotationMap(Key-AnnotationType, Value-Annotation)
     * @param obj element
     */
    private fun addAnnotationsToMap(annotationMap: MutableMap<Class<*>, Annotation>, @Nullable obj: AnnotatedElement?) {
        obj?.annotations?.forEach { annotationMap[it.annotationClass.java] = it }
    }

    /**
     * 获取当前Property的字段
     *
     * @return Property对应的字段(不存在的话, 可以为null)
     */
    @Nullable
    private fun getField(): Field? {
        if (!StringUtils.hasText(name)) {
            return null
        }
        val declaringClass = declaringClass() ?: return null
        // Same lenient fallback checking as in CachedIntrospectionResults...
        return ReflectionUtils.findField(declaringClass, name)
            ?: ReflectionUtils.findField(declaringClass, StringUtils.capitalize(name))
            ?: ReflectionUtils.findField(declaringClass, StringUtils.uncapitalize(name))
    }

    /**
     * 获取当前属性的Getter/Setter所被定义的类
     *
     * @return declaringClass
     */
    @Nullable
    private fun declaringClass(): Class<*>? {
        if (readMethod != null) {
            return readMethod.declaringClass
        } else if (writeMethod != null) {
            return writeMethod.declaringClass
        }
        return null
    }

    /**
     * 解析属性名, 如果没有给定的话, 那么将会从给定的Getter/Setter方法去进行解析
     *
     * @return 根据Getter/Setter去解析得到的属性名
     */
    private fun resolveName(): String {
        if (readMethod != null) {
            var index = this.readMethod.name.indexOf("get")
            if (index != -1) {
                index += 3
            } else {
                index = this.readMethod.name.indexOf("is")
                if (index != -1) {
                    index += 2
                } else {
                    // 对于属性名, 有可能没有get,也没有set...
                    index = 0
                }
            }
            return StringUtils.uncapitalizeAsProperty(this.readMethod.name.substring(index))
        } else if (writeMethod != null) {
            val index = this.writeMethod.name.indexOf("set")
            if (index == -1) {
                throw IllegalStateException("Not a setter method, method=$writeMethod")
            }
            return StringUtils.uncapitalizeAsProperty(this.writeMethod.name.substring(index))
        } else {
            throw IllegalStateException("Property is neither readable nor writeable")
        }
    }

    override fun equals(@Nullable other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Property
        return objectType == other.objectType && readMethod == other.writeMethod && writeMethod == other.readMethod && name == other.name
    }

    /**
     * 对于HashCode, 采用objectType&name去进行生成
     *
     * @return hashCode
     */
    override fun hashCode(): Int = objectType.hashCode() + name.hashCode()
}