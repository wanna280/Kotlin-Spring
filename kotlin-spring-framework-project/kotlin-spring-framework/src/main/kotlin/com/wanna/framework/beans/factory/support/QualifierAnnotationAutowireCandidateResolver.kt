package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.beans.factory.annotation.Value
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.context.annotation.AnnotationAttributesUtils
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.util.ClassUtils

/**
 * 这是一个支持@Qualifier/@Value注解的AutowireCandidateResolver
 */
open class QualifierAnnotationAutowireCandidateResolver : GenericTypeAwareAutowireCandidateResolver() {

    /**
     * Value的注解类型
     */
    private var valueAnnotationType: Class<out Annotation> = Value::class.java

    companion object {
        /**
         * Qualifier的注解列表，包括来自于javax.annotation包下的的Qualifier注解，也包括来自于Spring家的Qualifier
         */
        @JvmStatic
        private val qualifierAnnotationTypes: MutableList<Class<out Annotation>> = ArrayList()

        init {
            qualifierAnnotationTypes.add(Qualifier::class.java)
            try {
                qualifierAnnotationTypes.add(ClassUtils.forName("javax.inject.Qualifier"))
            } catch (ignored: ClassNotFoundException) {
                // 找不到javax.annotation.Qualifier(JSR-330)类就算了，ignore...
            }
        }
    }

    /**
     * 添加自定义的@Value注解
     *
     * @param annotationClass 想要标识为Value注解的注解类型
     */
    open fun setValueAnnotationType(annotationClass: Class<out Annotation>) {
        this.valueAnnotationType = annotationClass
    }

    /**
     * 添加自定义的@Qualifier注解
     *
     * @param annotationClass 想要标识为Qualifier注解的注解类型
     */
    open fun addQualifierType(annotationClass: Class<out Annotation>) {
        qualifierAnnotationTypes += annotationClass
    }

    /**
     * 如果描述符当中给定了required=false，那么就return false；如果required=true，那么就得检查一下Autowired注解；
     * 如果依赖描述符上给定的required=true，并且Autowired上required=false，那么也得return false；
     *
     * @param descriptor 依赖描述符
     * @return 该依赖是否是必须的？
     */
    override fun isRequired(descriptor: DependencyDescriptor): Boolean {
        val autowired = descriptor.getAnnotation(Autowired::class.java)
        return if (!super.isRequired(descriptor)) false else autowired == null || autowired.required
    }

    /**
     *
     * ## 1.针对有Qualifier注解的情况
     * 在这个方法当中，我们将对@Qualifier注解去进行匹配，如果Autowire的元素(字段/方法参数/方法/构造器参数/构造器)上有Qualifier属性，
     * 并且在候选的BeanDefinition(bdHolder)对应的Bean当中也有Qualifier，那么将Qualifier去进行equals匹配，如果匹配直接return true；
     * 如果还是不匹配的话，那么拿出Qualifier的"value"属性出来，和bdHolder.beanName匹配，如果匹配的话，return true...
     * 也就是说支持去使用@Qualifier的方式去进行匹配Bean，也支持使用beanName的方式去进行匹配
     *
     * ## 2.针对于没有Qualifier注解的情况...
     * 只要候选的Bean的类型匹配，那么一律视为匹配！(父类当中处理)
     *
     * @param bdHolder 候选Bean对应的BeanDefinition
     * @param descriptor 依赖描述符
     * @return 是否匹配？
     */
    override fun isAutowireCandidate(bdHolder: BeanDefinitionHolder, descriptor: DependencyDescriptor): Boolean {
        if (!super.isAutowireCandidate(bdHolder, descriptor)) {
            return false
        }

        // 1. 检查方法参数/字段上的Qualifier
        var match = checkQualifiers(bdHolder, descriptor.getAnnotations())
        if (match) {
            val parameter = descriptor.getMethodParameter()
            // 如果方法参数不为null，说明它可能是一个构造器/方法，一定不是一个字段
            if (parameter != null) {
                val method = parameter.getMethod()
                // 如果method==null(说明它是一个构造器)，或者该方法的返回值是void，那么还得去构造器或者是方法上找一找注解...
                if (method == null || method.returnType == Unit::class.java) {
                    match = checkQualifiers(bdHolder, parameter.getMethodAnnotations())
                }
            }
        }
        return match
    }

    /**
     * 遍历所有要去进行匹配的注解，获取到Qualifier注解，去进行匹配；没有Qualifier注解的，只要类型是匹配的，那么一律return true
     *
     * @param bdHolder 候选Bean的BeanDefinition
     * @param annotationsToMatch 要去进行注入的元素上的全部注解列表
     * @return 是否匹配？(没有Qualifier注解的，一律return true，有Qualifier注解的，那么就得匹配)
     */
    protected open fun checkQualifiers(bdHolder: BeanDefinitionHolder, annotationsToMatch: Array<Annotation>): Boolean {
        if (annotationsToMatch.isEmpty()) {
            return true
        }
        annotationsToMatch.forEach {
            var checkMeta = true
            var fallbackToMeta = false
            val annotationClass = it.annotationClass.java
            if (isQualifier(annotationClass)) {
                if (!checkQualifier(bdHolder, it)) {
                    fallbackToMeta = true  // 匹配失败，要去检查元信息的情况
                } else {
                    checkMeta = false  // 匹配成功，那么就不去检查元信息了
                }
            }
            // 如果之前没有匹配Qualifier的话，尝试去二级注解上去找
            if (checkMeta) {
                var found = false
                annotationClass.annotations.forEach { meta ->
                    val metaType = meta.annotationClass.java
                    if (isQualifier(metaType)) {
                        found = true
                        val attributes = AnnotationAttributesUtils.asAnnotationAttributes(meta)

                        // 如果之前匹配Qualifier的时候失败了(Qualifier找到了，说明就是Qualifier不匹配的情况了)，这里还失败那么肯定失败...
                        if (fallbackToMeta && attributes!![MergedAnnotation.VALUE] == "" && !checkQualifier(bdHolder, meta)) {
                            return false
                        }
                    }
                }
                // 如果第一次检测确实是失败了，去二级注解上还没找到Qualifier注解，那么肯定return false
                if (fallbackToMeta && !found) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * 判断Qualifier和beanName或者成对的Qualifier是否匹配？
     *
     * @param bdHolder beanDefinition
     * @param annotation Qualifier注解(实例)
     */
    protected open fun checkQualifier(bdHolder: BeanDefinitionHolder, annotation: Annotation): Boolean {
        // 匹配类上，或者是FactoryMethod上的Qualifier注解信息
        val beanDefinition = bdHolder.beanDefinition as RootBeanDefinition
        val annotationClass = annotation.annotationClass.java

        var targetAnnotation: Annotation? = null
        val beanClass = beanDefinition.getBeanClass()
        if (beanClass != null) {
            targetAnnotation = AnnotatedElementUtils
                .getMergedAnnotation(ClassUtils.getUserClass(beanClass), annotationClass)
        }
        if (beanDefinition.getFactoryMethodName() != null && targetAnnotation == null && beanDefinition.getResolvedFactoryMethod() != null) {
            val factoryMethod = beanDefinition.getResolvedFactoryMethod()!!
            targetAnnotation = AnnotatedElementUtils.getMergedAnnotation(factoryMethod, annotationClass)
        }

        // 如果成对匹配的话(equals)，return true
        if (targetAnnotation != null && targetAnnotation == annotation) {
            return true
        }

        // 如果没有成对匹配的话，那么需要fallback去匹配Qualifier注解与beanName的情况...
        val attributes = AnnotationAttributesUtils.asNonNullAnnotationAttributes(annotation)
        attributes.forEach { (k, v) ->
            if (k == MergedAnnotation.VALUE && bdHolder.matchesName(v.toString())) {
                return true
            }
        }
        return false
    }

    /**
     * 判断要进行注入的依赖的DependencyDescriptor上是否有Qualifier注解(Spring家的Qualifier和javax.inject包下的Qualifier)
     *
     * @param descriptor 依赖描述符
     * @return 依赖描述符当中的注解是否存在有Qualifier注解？
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
     *
     * @param descriptor 依赖描述符
     * @return 建议去设置的值，有@Value注解。return @Value的value属性，如果@Value注解没有，那么return null
     */
    override fun getSuggestedValue(descriptor: DependencyDescriptor): Any? {
        // 1.获取从方法参数/构造器参数/字段上的@Value注解的value属性
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
     * 判断是否是Qualifier注解(包括Spring家的Qualifier以及javax.inject包下的Qualifier)，如果找到了return true；没找到，return false
     *
     * @param annotationClass 目标注解类型
     * @return 如果目标注解类型是Qualifier，那么return true；否则return false
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
     *
     * @param annotations 候选的注解列表
     * @return 找到的@Value注解的value属性，如果找不到return null
     */
    private fun findValue(annotations: Array<Annotation>): Any? {
        for (annotation in annotations) {
            if (valueAnnotationType == annotation.annotationClass.java) {
                return extractValue(AnnotationAttributesUtils.asNonNullAnnotationAttributes(annotation))
            }
        }
        return null
    }

    /**
     * 从给定的Value注解上找到它的value属性去进行return，如果value属性为空，那么抛出异常...
     *
     * @param annotationAttributes @Value注解信息
     * @return 在@Value注解上找到的value属性
     */
    private fun extractValue(annotationAttributes: AnnotationAttributes?): Any {
        return annotationAttributes?.getString(MergedAnnotation.VALUE)
            ?: throw IllegalStateException("@Value注解上的value属性没有给出！")
    }
}