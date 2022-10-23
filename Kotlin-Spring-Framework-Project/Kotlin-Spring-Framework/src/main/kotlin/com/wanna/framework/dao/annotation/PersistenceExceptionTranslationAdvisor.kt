package com.wanna.framework.dao.annotation

import com.wanna.framework.aop.support.AbstractPointcutAdvisor
import com.wanna.framework.aop.support.annotation.AnnotationMatchingPointcut
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.dao.support.PersistenceExceptionTranslationInterceptor

/**
 * 持久层的异常翻译器的PointcutAdvisor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class PersistenceExceptionTranslationAdvisor(
    beanFactory: ListableBeanFactory,
    repositoryAnnotationType: Class<out Annotation>
) : AbstractPointcutAdvisor() {

    /**
     * Advice，提供使用SpringAOP的方式对于目标方法的拦截功能
     */
    private var advice = PersistenceExceptionTranslationInterceptor(beanFactory)

    /**
     * Pointcut，提供对于@Repository注解的匹配
     */
    private var pointcut = AnnotationMatchingPointcut(repositoryAnnotationType)

    /**
     * 为父类提供Advice
     */
    override fun getAdvice() = this.advice

    /**
     * 为父类提供Pointcut，提供对于注解的匹配
     */
    override fun getPointcut() = this.pointcut
}