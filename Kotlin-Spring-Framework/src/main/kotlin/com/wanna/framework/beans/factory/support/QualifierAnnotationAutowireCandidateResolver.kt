package com.wanna.framework.beans.factory.support

import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.context.util.AnnotationAttributesUtils
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.beans.factory.annotation.Value
import com.wanna.framework.core.util.ClassUtils

/**
 * 这是一个支持Qualifier注解的AutowireCandidateResolver
 */
open class QualifierAnnotationAutowireCandidateResolver : GenericTypeAwareAutowireCandidateResolver() {

    // value的注解类型
    var valueAnnotationType: Class<out Annotation> = Value::class.java

    companion object {
        // Qualifier的注解列表
        private val qualifierAnnotationTypes: MutableList<Class<out Annotation>> = ArrayList()

        init {
            qualifierAnnotationTypes.add(Qualifier::class.java)
            try {
                qualifierAnnotationTypes.add(ClassUtils.forName("javax.inject.Qualifier"))
            } catch (ignored: ClassNotFoundException) {
                // 找不到类就算了，ingore
            }
        }
    }

    /**
     * 如果描述符当中给定了required=false，那么就return false；如果required=true，那么就得检查一下Autowired注解
     */
    override fun isRequired(descriptor: DependencyDescriptor): Boolean {
        val autowired = descriptor.getAnnotation(Autowired::class.java)
        return if (!super.isRequired(descriptor)) false else autowired == null || autowired.required
    }

    /**
     * 判断要进行注入的依赖的DependencyDescriptor上是否有Qualifier注解
     */
    override fun hasQualifier(descriptor: DependencyDescriptor): Boolean {
        for (annotation in descriptor.getAnnotations()) {
            if (isQualifier(annotation.annotationClass.java)) {
                return true
            }
        }
        return false
    }

    /**
     * 获取建议的值，从@Value注解上去寻找value属性，如果找到了return找到的值，如果没有找到，return null
     */
    override fun getSuggestedValue(descriptor: DependencyDescriptor): Any? {
        // 1.获取从方法参数/字段上的@Value注解的value属性
        var value = findValue(descriptor.getAnnotations())
        if (value == null) {
            val methodParameter = descriptor.getMethodParameter()
            // 2.从方法参数对应的方法上去找@Value注解
            if (methodParameter != null) {
                value = findValue(methodParameter.getMethodAnnotations())
            }
        }
        return value
    }


    /**
     * 判断是否是Qualifier注解，如果找到了return true；没找到，return false
     */
    private fun isQualifier(annotationClass: Class<out Annotation>): Boolean {
        for (qualifierAnnotationType in qualifierAnnotationTypes) {
            if (annotationClass == qualifierAnnotationType) {
                return true
            }
        }
        return false
    }

    /**
     * 在给定的注解列表当中去寻找@Value注解的值，如果找不到，return null
     */
    private fun findValue(annotations: Array<Annotation>): Any? {
        for (annotation in annotations) {
            if (valueAnnotationType == annotation.annotationClass.java) {
                return extractValue(AnnotationAttributesUtils.asAnnotationAttributes(annotation))
            }
        }
        return null
    }

    /**
     * 从给定的Value注解上找到它的value属性去进行return，如果value属性为空，那么抛出异常...
     */
    private fun extractValue(annotationAttributes: AnnotationAttributes?): Any {
        return annotationAttributes?.getString("value") ?: throw IllegalStateException("@Value注解上的value属性没有给出！")
    }
}