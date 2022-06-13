package com.wanna.framework.transaction.annotation

import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.context.annotation.AnnotationAttributesUtils
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.ImportAware
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.transaction.TransactionManager

/**
 * 提供了@EnableTransactionManagement的相关信息以及从TransactionManagementConfigurer获取TransactionManager的相关支持
 *
 * @see ProxyTransactionManagementConfiguration
 */
abstract class AbstractTransactionManagementConfiguration : ImportAware {
    // @EnableTransactionManagement的相关属性
    protected var enableTx: AnnotationAttributes? = null

    // Spring事务的TransactionManager，可以没有，直接从BeanFactory当中去进行获取
    protected var transactionManager: TransactionManager? = null

    override fun setImportMetadata(annotationMetadata: AnnotationMetadata) {
        val attributes = annotationMetadata.getAnnotationAttributes(EnableTransactionManagement::class.java)
        if (attributes.isEmpty()) {
            throw IllegalStateException("没有从目标类[${annotationMetadata.getClassName()}]上找到@EnableTransactionManagement注解信息")
        }
        this.enableTx = AnnotationAttributesUtils.fromMap(attributes)
    }


    @Autowired(required = false)
    fun setConfigurers(configurers: Collection<TransactionManagementConfigurer>) {
        if (configurers.isEmpty()) {
            return
        }
        if (configurers.size > 1) {
            throw IllegalStateException("Spring BeanFactory当中的TransactionManagementConfigurer不止1个，但是只允许有一个")
        }
        val configurer = configurers.iterator().next()
        this.transactionManager = configurer.annotationDrivenTransactionManager()
    }
}