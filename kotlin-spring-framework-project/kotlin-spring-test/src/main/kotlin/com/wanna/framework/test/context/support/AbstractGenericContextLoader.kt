package com.wanna.framework.test.context.support

import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.BeanDefinitionReader
import com.wanna.framework.context.support.GenericApplicationContext
import com.wanna.framework.test.context.ContextConfigurationAttributes
import com.wanna.framework.test.context.MergedContextConfiguration
import com.wanna.framework.test.context.SmartContextLoader
import com.wanna.framework.util.AnnotationConfigUtils

/**
 * 创建一个[GenericApplicationContext]作为[ApplicationContext]的[SmartContextLoader]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
abstract class AbstractGenericContextLoader : SmartContextLoader {

    override fun processLocations(vararg locations: String): Array<String> = arrayOf(*locations)

    override fun processContextConfiguration(configAttributes: ContextConfigurationAttributes) {

    }

    /**
     * 根据Spring的XML配置路径去加载得到[ApplicationContext]
     *
     * @param locations XML配置文件路径
     * @return 加载得到的[ApplicationContext]
     */
    final override fun loadContext(vararg locations: String): ApplicationContext {
        val context = GenericApplicationContext()

        // 准备ApplicationContext
        prepareContext(context)

        // 自定义BeanFactory
        customizeBeanFactory(context.getDefaultListableBeanFactory())

        // loadBeanDefinitions(XML)
        createBeanDefinitionReader(context).loadBeanDefinitions(*locations)

        // 对ApplicationContext去进行自定义
        customizeContext(context)

        // 注册一些处理注解配置的BeanPostProcessor
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context)

        // 刷新ApplicationContext
        context.refresh()
        return context
    }

    /**
     * 根据[MergedContextConfiguration]去加载得到[ApplicationContext]
     *
     * @param mergedContextConfiguration MergedContextConfiguration
     * @return 完成初始化和刷新之后的ApplicationContext
     */
    final override fun loadContext(mergedContextConfiguration: MergedContextConfiguration): ApplicationContext {
        val context = GenericApplicationContext()

        // 准备ApplicationContext
        prepareContext(context)
        prepareContext(context, mergedContextConfiguration)

        // 自定义BeanFactory
        customizeBeanFactory(context.getDefaultListableBeanFactory())

        // loadBeanDefinitions
        loadBeanDefinitions(context, mergedContextConfiguration)

        // 对ApplicationContext去进行自定义
        customizeContext(context)
        customizeContext(context, mergedContextConfiguration)

        // 注册一些处理注解配置的BeanPostProcessor
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context)

        // 刷新ApplicationContext
        context.refresh()
        return context
    }

    /**
     * 根据[MergedContextConfiguration]去加载[BeanDefinition]到[ApplicationContext]当中来
     *
     * @param context ApplicationContext
     * @param mergedContextConfiguration MergedContextConfiguration
     */
    protected open fun loadBeanDefinitions(
        context: GenericApplicationContext,
        mergedContextConfiguration: MergedContextConfiguration
    ) {
        createBeanDefinitionReader(context).loadBeanDefinitions(*mergedContextConfiguration.getLocations())
    }

    /**
     * 准备[ApplicationContext]
     *
     * @param context ApplicationContext
     * @param mergedContextConfiguration MergedContextConfiguration
     */
    protected open fun prepareContext(
        context: GenericApplicationContext,
        mergedContextConfiguration: MergedContextConfiguration
    ) {

    }

    /**
     * 对于[ApplicationContext]去进行自定义
     *
     * @param context context
     */
    protected open fun customizeContext(
        context: GenericApplicationContext,
        mergedContextConfiguration: MergedContextConfiguration
    ) {

    }


    protected abstract fun createBeanDefinitionReader(context: GenericApplicationContext): BeanDefinitionReader

    /**
     * 准备[ApplicationContext], 给子类一个机会去进行自定义
     *
     * @param applicationContext ApplicationContext
     */
    protected open fun prepareContext(applicationContext: ApplicationContext) {

    }

    /**
     * 给子类一个机会, 去对[DefaultListableBeanFactory]去进行自定义
     *
     * @param beanFactory BeanFactory
     */
    protected open fun customizeBeanFactory(beanFactory: DefaultListableBeanFactory) {

    }

    /**
     * 给子类一个机会, 对于[ApplicationContext]去进行自定义
     *
     * @param context context
     */
    protected open fun customizeContext(context: GenericApplicationContext) {

    }
}