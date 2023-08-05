package com.wanna.framework.transaction.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Role
import com.wanna.framework.transaction.config.TransactionManagementConfigUtils
import com.wanna.framework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor
import com.wanna.framework.transaction.interceptor.TransactionAttributeSource
import com.wanna.framework.transaction.interceptor.TransactionInterceptor
import java.util.Optional

/**
 * Spring事务需要用到的配置类, 负责给SpringBeanFactory当中导入Spring事务需要用到的Advisor/Advice, 以及@Transactional的注解属性匹配
 *
 * @see AbstractTransactionManagementConfiguration
 * @see BeanFactoryTransactionAttributeSourceAdvisor
 * @see AnnotationTransactionAttributeSource
 * @see TransactionInterceptor
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration(proxyBeanMethods = false)
open class ProxyTransactionManagementConfiguration : AbstractTransactionManagementConfiguration() {

    /**
     * SpringTransaction的Advisor, 负责去匹配@Transactional注解生成Advisor, 
     * 并使用TransactionInterceptor作为拦截的callback基于SpringAop去生成代理; 
     *
     * @see TransactionInterceptor
     */
    @Bean(TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    open fun transactionAdvisor(
        transactionAttributeSource: TransactionAttributeSource, transactionInterceptor: TransactionInterceptor
    ): BeanFactoryTransactionAttributeSourceAdvisor {
        val transactionAdvisor = BeanFactoryTransactionAttributeSourceAdvisor()
        transactionAdvisor.setTransactionAttributeSource(transactionAttributeSource)
        transactionAdvisor.setAdvice(transactionInterceptor)  // setAdvice
        Optional.ofNullable(this.enableTx).ifPresent { transactionAdvisor.setOrder(it.getInt("order")) }
        return transactionAdvisor
    }

    /**
     * SpringTransaction的事务属性源, 提供事务属性的匹配功能, 使用各个策略, 从@Transactional注解当中去获取相关的信息; 
     * 不管是启动时匹配一个目标类是否要生成事务代理, 还是运行时获取事务属性信息, 都都需要获取到该组件从@Transactional当中去获取到事务属性信息
     *
     * @see AnnotationTransactionAttributeSource
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    open fun transactionAttributeSource(): TransactionAttributeSource {
        return AnnotationTransactionAttributeSource()
    }

    /**
     * 给SpringBeanFactory当中导入一个TransactionInterceptor, 负责去处理目标方法
     *
     * * 1.设置TransactionManager, 整个事务想要使用的事务同步管理器
     * * 2.设置TransactionAttributeSource, 负责获取到@Transactional注解当中的属性信息
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    open fun transactionInterceptor(transactionAttributeSource: TransactionAttributeSource): TransactionInterceptor {
        val transactionInterceptor = TransactionInterceptor()
        Optional.ofNullable(this.transactionManager).ifPresent { transactionInterceptor.setTransactionManager(it) }
        transactionInterceptor.setTransactionAttributeSource(transactionAttributeSource)
        return transactionInterceptor
    }
}