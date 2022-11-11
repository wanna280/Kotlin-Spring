package com.wanna.framework.transaction.annotation

import com.wanna.framework.context.annotation.AutoProxyRegistrar
import com.wanna.framework.context.annotation.ImportSelector
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * Spring事务的ImportSelector，负责导入Spring事务相关的组件
 *
 * * 1.AutoProxyRegistrar，负责解析@EnableTransactionManagement注解，并注册SpringAop的代理创建器组件
 * * 2.ProxyTransactionManagementConfiguration，为SpringAop提供Advisor等支持，方便SpringAop能针对Spring事务提供支持
 *
 * @see AutoProxyRegistrar
 * @see ProxyTransactionManagementConfiguration
 */
open class TransactionManagementConfigurationSelector : ImportSelector {
    override fun selectImports(metadata: AnnotationMetadata): Array<String> {
        return arrayOf(AutoProxyRegistrar::class.java.name, ProxyTransactionManagementConfiguration::class.java.name)
    }
}