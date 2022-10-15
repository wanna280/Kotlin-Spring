package com.wanna.framework.dao.annotation

import com.wanna.framework.aop.framework.AbstractBeanFactoryAwareAdvisingPostProcessor
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.context.stereotype.Repository

/**
 * 提供对于持久层的异常翻译器的BeanPostProcessor，对于标注了@Repostory注解的类去提供SpringAOP的切面增强功能；
 * 支持对目标方法抛出来的异常去进行转换，成为DataAccessException
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 * @see PersistenceExceptionTranslationAdvisor
 * @see Repository
 * @see com.wanna.framework.dao.DataAccessException
 * @see com.wanna.framework.dao.support.PersistenceExceptionTranslator
 */
open class PersistenceExceptionTranslationPostProcessor : AbstractBeanFactoryAwareAdvisingPostProcessor() {

    /**
     * Repository的注解，var关键字自带Getter和Setter，支持用户去进行自定义
     */
    var repositoryAnnotationType: Class<out Annotation> = Repository::class.java

    /**
     * 重写setBeanFactory方法，为父类提供Advisor功能，为合适的类去创建SpringAOP代理
     */
    override fun setBeanFactory(beanFactory: BeanFactory) {
        super.setBeanFactory(beanFactory)
        if (beanFactory !is ListableBeanFactory) {
            throw IllegalStateException("不支持使用非ListableBeanFactory去进行PersistenceExceptionTranslator的自动探测")
        }
        this.advisor =
            PersistenceExceptionTranslationAdvisor(beanFactory, repositoryAnnotationType)
    }
}