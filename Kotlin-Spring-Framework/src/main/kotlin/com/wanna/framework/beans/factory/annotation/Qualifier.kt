package com.wanna.framework.beans.factory.annotation

import com.wanna.framework.context.annotation.Autowired

/**
 * 标识这是一个限定符，限定了Autowire的元素(不仅是可以标注在Autowired上，也可以是别的自动注入的注解，比如@Inject/@Resource等都可以支持)要去进行注入的beanName；
 * 当然也可以标注在beanClass上，例如B类上有Qualifier注解，并且A想要去注入B，并且在b字段上加了Qualifier；
 * 此时Spring支持将b当中的Qualifier和A类上的Qualifier去进行匹配，如果类A找不到Qualifier，才会使用beanName去进行匹配
 *
 * 实际上，Qualifier也可以标注在FactoryMethod(@Bean方法)上，和标在类上作用相同；
 *
 * 它会被QualifierAnnotationAutowireCandidateResolver.checkQualifiers当中所处理
 *
 * @see com.wanna.framework.beans.factory.support.QualifierAnnotationAutowireCandidateResolver.checkQualifiers
 * @see com.wanna.framework.beans.factory.support.DefaultListableBeanFactory.isAutowireCandidate
 * @see com.wanna.framework.beans.factory.support.DefaultListableBeanFactory.autowireCandidateResolver
 *
 * @see com.wanna.framework.context.annotation.Autowired
 * @see javax.annotation.Resource
 * @see javax.annotation.Inject
 */
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.TYPE_PARAMETER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.TYPE,
    AnnotationTarget.CLASS
)
annotation class Qualifier(val value: String)
