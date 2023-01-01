package com.wanna.framework.transaction.annotation

import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.context.annotation.AnnotationAttributesUtils
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.ImportAware
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.transaction.TransactionManager

/**
 * 提供了@EnableTransactionManagement的相关信息以及从TransactionManagementConfigurer获取TransactionManager的相关支持
 *
 * @see ProxyTransactionManagementConfiguration
 */
abstract class AbstractTransactionManagementConfiguration : ImportAware {

    /**
     * 描述的是@EnableTransactionManagement的注解当中的各个属性
     */
    protected var enableTx: MergedAnnotation<*>? = null

    /**
     * Spring事务的TransactionManager，可以没有，直接从BeanFactory当中去进行获取
     */
    protected var transactionManager: TransactionManager? = null

    /**
     * 自动注入导入这个配置类的注解信息，去获取到@EnableTransactionManagement的注解当中的各个属性去进行保存
     *
     * @param annotationMetadata 注解元信息
     */
    override fun setImportMetadata(annotationMetadata: AnnotationMetadata) {
        val attributes = annotationMetadata.getAnnotations().get(EnableTransactionManagement::class.java)
        if (!attributes.present) {
            throw IllegalStateException("没有从目标类[${annotationMetadata.getClassName()}]上找到@EnableTransactionManagement注解信息")
        }
        this.enableTx = attributes
    }


    /**
     * 自动注入容器中所有的TransactionManagementConfigurer，对transactionManager去进行自定义
     *
     * @throws IllegalStateException 如果容器中的TransactionManagementConfigurer数量不止一个
     */
    @Autowired(required = false)
    open fun setConfigurers(configurers: Collection<TransactionManagementConfigurer>) {
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