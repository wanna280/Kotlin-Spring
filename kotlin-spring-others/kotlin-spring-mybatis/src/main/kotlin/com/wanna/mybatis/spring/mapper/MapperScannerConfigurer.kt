package com.wanna.mybatis.spring.mapper

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.processor.factory.BeanDefinitionRegistryPostProcessor
import com.wanna.framework.util.StringUtils
import com.wanna.mybatis.spring.SqlSessionTemplate
import org.apache.ibatis.session.SqlSessionFactory
import java.util.Optional

/**
 * MapperScan的Configurer, 负责真正地对@MapperScan去进行处理
 */
open class MapperScannerConfigurer : BeanDefinitionRegistryPostProcessor {
    var basePackages: String? = null

    var addToConfig = true

    var lazyInitialization: String? = null

    var sqlSessionFactory: SqlSessionFactory? = null

    var sqlSessionTemplate: SqlSessionTemplate? = null

    var sqlSessionFactoryBeanName: String? = null

    var sqlSessionTemplateBeanName: String? = null

    var annotationClass: Class<out Annotation?>? = null

    var markerInterface: Class<*>? = null

    var mapperFactoryBeanClass: Class<out MapperFactoryBean?>? = null

    var applicationContext: ApplicationContext? = null

    var beanName: String? = null

    var processPropertyPlaceHolders = false

    var nameGenerator: BeanNameGenerator? = null

    var defaultScope: String? = null

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        if (processPropertyPlaceHolders) {
            processPropertyPlaceHolders()
        }
        val scanner = ClassPathMapperScanner(registry)
        scanner.setBeanNameGenerator(this.nameGenerator)
        if (StringUtils.hasText(lazyInitialization)) {
            scanner.lazyInitialization = lazyInitialization!!.toBoolean()
        }
        Optional.ofNullable(this.mapperFactoryBeanClass).ifPresent { scanner.mapperFactoryBeanClass = it }

        scanner.defaultScope = defaultScope
        scanner.sqlSessionTemplateBeanName = sqlSessionTemplateBeanName
        scanner.sqlSessionFactoryBeanName = sqlSessionFactoryBeanName
        scanner.sqlSessionFactory = sqlSessionFactory
        scanner.sqlSessionTemplate = sqlSessionTemplate
        scanner.annotationClass = annotationClass
        scanner.markerInterface = markerInterface
        scanner.addToConfig = addToConfig

        // 注册相应的匹配的TypeFilter
        scanner.registerFilters()

        // 开始去按照指定的规则去进行扫描
        scanner.scan(*StringUtils.commaDelimitedListToStringArray(basePackages))
    }

    private fun processPropertyPlaceHolders() {

    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {

    }
}