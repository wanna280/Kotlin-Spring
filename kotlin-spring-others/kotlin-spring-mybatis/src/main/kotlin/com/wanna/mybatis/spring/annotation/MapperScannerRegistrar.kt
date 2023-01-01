package com.wanna.mybatis.spring.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.context.annotation.AnnotationAttributesUtils
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.annotation.ImportBeanDefinitionRegistrar
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import com.wanna.mybatis.spring.mapper.MapperFactoryBean
import com.wanna.mybatis.spring.mapper.MapperScannerConfigurer

/**
 * MapperScan的注册器，负责处理@MapperScan注解，将一个MapperScan的相关信息注册成为一个MapperScanConfigurer，交给它去完成@MapperScan的处理
 *
 * @see MapperScan
 */
open class MapperScannerRegistrar : ImportBeanDefinitionRegistrar {

    override fun registerBeanDefinitions(annotationMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val attributes = annotationMetadata.getAnnotations().get(MapperScan::class.java)
        if (attributes.present) {
            registerBeanDefinitions(
                annotationMetadata, registry, attributes, generateBaseBeanName(annotationMetadata, 0)
            )
        }
    }


    /**
     * 将全部的属性信息封装成为MapperScannerConfigurer的BeanDefinition，相关的属性配置到BeanDefinition的PropertyValues当中，
     * 并将其BeanDefinition注册到BeanDefinitionRegistry当中(MapperScannerConfigurer是一个BeanDefinitionRegistryPostProcessor)；
     * 在对MapperScannerConfigurer去进行创建对象时，会自动将这些属性值注入到MapperScannerConfigurer的各个属性当中
     *
     * @param importClassMetadata MapperScan注解的导入类的元信息
     * @param registry BeanDefinitionRegistry
     * @param attributes 注解属性信息
     * @param beanName MapperScannerConfigurer的beanName
     */
    protected open fun registerBeanDefinitions(
        importClassMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry,
        attributes: MergedAnnotation<*>,
        beanName: String
    ) {
        val beanDefinition = GenericBeanDefinition(MapperScannerConfigurer::class.java)
        val propertyValues = beanDefinition.getPropertyValues()

        // processPropertyPlaceHolders to true
        propertyValues.addPropertyValue("processPropertyPlaceHolders", true)

        // 设置annotationClass
        val annotationClass = attributes.getClass("annotationClass")
        if (annotationClass != Annotation::class.java) {
            propertyValues.addPropertyValue("annotationClass", annotationClass)
        }

        // set MarkerInterface
        val markerInterface = attributes.getClass("markerInterface")
        if (markerInterface != Class::class.java) {
            propertyValues.addPropertyValue("markerInterface", markerInterface)
        }

        // add BeanNameGenerator
        val nameGenerator = attributes.getClass("nameGenerator")
        if (nameGenerator != BeanNameGenerator::class.java) {
            propertyValues.addPropertyValue("nameGenerator", BeanUtils.instantiateClass(nameGenerator))
        }

        // set MapperFactoryBean
        val factoryBean = attributes.getClass("factoryBean")
        if (factoryBean != MapperFactoryBean::class.java) {
            propertyValues.addPropertyValue("mapperFactoryBeanClass", factoryBean)
        }

        // set sqlSessionTemplateRef
        val sqlSessionTemplateRef = attributes.getString("sqlSessionTemplateRef")
        if (StringUtils.hasText(sqlSessionTemplateRef)) {
            propertyValues.addPropertyValue("sqlSessionTemplateBeanName", sqlSessionTemplateRef)
        }

        // set SqlSessionFactoryBean
        val sqlSessionFactoryRef = attributes.getString("sqlSessionFactoryRef")
        if (StringUtils.hasText(sqlSessionFactoryRef)) {
            propertyValues.addPropertyValue("sqlSessionFactoryBeanName", sqlSessionFactoryRef)
        }

        // 添加basePackages
        val basePackages = ArrayList<String>()
        attributes.getStringArray("value").filter { StringUtils.hasText(it) }.forEach(basePackages::add)
        attributes.getStringArray("basePackages").filter { StringUtils.hasText(it) }.forEach(basePackages::add)
        attributes.getClassArray("basePackageClasses").map { it.packageName }.forEach(basePackages::add)
        if (basePackages.isEmpty()) {  // 如果必要的话，需要获取默认的basePackages
            basePackages.add(getDefaultBasePackage(importClassMetadata))
        }
        propertyValues.addPropertyValue("basePackages", StringUtils.collectionToCommaDelimitedString(basePackages))

        // 解析是否要懒加载的属性？
        val lazyInitialization = attributes.getString("lazyInitialization")
        if (StringUtils.hasText(lazyInitialization)) {
            propertyValues.addPropertyValue("lazyInitialization", lazyInitialization)
        }

        // 如果必要的话，设置scope
        val defaultScope = attributes.getString("defaultScope")
        if (StringUtils.hasText(defaultScope)) {
            propertyValues.addPropertyValue("defaultScope", defaultScope)
        }

        beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
        registry.registerBeanDefinition(beanName, beanDefinition)
    }

    /**
     * 使用合适的生成策略去生成MapperScannerConfigurer的beanName
     *
     * @param importClassMetadata MapperScan的注解元信息
     * @param index index
     * @return 生成的beanName
     */
    private fun generateBaseBeanName(importClassMetadata: AnnotationMetadata, index: Int): String =
        importClassMetadata.getClassName() + "#" + MapperScannerRegistrar::class.java.simpleName + "#" + index

    /**
     * 获取MapperScan的默认包名
     *
     * @param annotationMetadata 标注@MapperScan的类的元信息
     */
    private fun getDefaultBasePackage(annotationMetadata: AnnotationMetadata): String =
        ClassUtils.getPackageName(annotationMetadata.getClassName())
}