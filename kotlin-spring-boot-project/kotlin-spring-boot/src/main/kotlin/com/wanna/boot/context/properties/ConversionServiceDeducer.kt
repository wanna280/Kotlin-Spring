package com.wanna.boot.context.properties

import com.wanna.boot.convert.ApplicationConversionService
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.beans.factory.annotation.BeanFactoryAnnotationUtils
import com.wanna.framework.beans.factory.annotation.BeanFactoryAnnotationUtils.qualifiedBeansOfType
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext.Companion.CONVERSION_SERVICE_BEAN_NAME
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.converter.Converter
import com.wanna.framework.core.convert.converter.ConverterRegistry
import com.wanna.framework.core.convert.converter.GenericConverter

/**
 * 根据[ApplicationContext]去推断出来合适的[ConversionService]的推断器工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/9
 */
class ConversionServiceDeducer(private val applicationContext: ApplicationContext) {

    /**
     * 根据[ApplicationContext]去推断出来合适的[ConversionService]
     *
     * @return ConversionServices
     */
    fun getConversionServices(): List<ConversionService> {
        // 如果有用户自定义的ConversionService的话, 那么返回用户自定义的ConversionService
        if (this.hasUserDefinedConfigurationServiceBean()) {
            return listOf(this.applicationContext.getBean(ConversionService::class.java))
        }
        // 如果没有用户自定义的ConversionService的话, 那么, 返回ApplicationContext当中的默认的ConversionService
        return if (this.applicationContext is ConfigurableApplicationContext) getConversionServices(this.applicationContext) else emptyList()
    }

    /**
     * 检查一下[ApplicationContext]当中是否有自定义的[ConversionService]的Bean
     *
     * @return 如果ApplicationContext当中有自定义的ConversionService的话, 那么return true; 否则return false
     */
    private fun hasUserDefinedConfigurationServiceBean(): Boolean {
        return this.applicationContext.containsBeanDefinition(CONVERSION_SERVICE_BEAN_NAME)
                && this.applicationContext.getAutowireCapableBeanFactory()
            .isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService::class.java)
    }

    /**
     * 从ApplicationContext当中去获取到合适的[ConversionService]
     *
     * * 1.添加ApplicationContext的默认的ConversionService
     * * 2.如果有`@ConfigurationPropertiesBinding`注解的Converter的话, 另外需要创建一个ApplicationConversionService去装那些Converter
     *
     * @param applicationContext ApplicationContext
     */
    private fun getConversionServices(applicationContext: ConfigurableApplicationContext): List<ConversionService> {
        val conversionServices = ArrayList<ConversionService>()

        // 如果ApplicationContext有默认的ConversionService的话, 那么先加入进去
        if (applicationContext.getBeanFactory().getConversionService() != null) {
            conversionServices.add(applicationContext.getBeanFactory().getConversionService()!!)
        }
        // 统计那些标注了@ConfigurationPropertiesBinding的注解的Converter/GenericConverter/...
        val converterBeans = ConverterBeans(applicationContext)

        // 如果有Converter的话, 那么把那些添加了@ConfigurationPropertiesBinding注解的Converter, 全部添加到ApplicationConversionService当中
        if (!converterBeans.isEmpty()) {
            val beanConversionService = ApplicationConversionService()

            // 将那些Converter转移到ApplicationConversionService当中来...
            converterBeans.addTo(beanConversionService)
            conversionServices.add(beanConversionService)
        }
        return conversionServices
    }

    /**
     * 从给定的[ApplicationContext]当中, 找到那些标注了`@ConfigurationPropertiesBinding`注解的Converter,
     * 并将这些Converter去添加到给定的[ConverterRegistry]当中去
     *
     * @param applicationContext ApplicationContext
     */
    private class ConverterBeans(applicationContext: ConfigurableApplicationContext) {

        /**
         * Converters
         */
        private val converters =
            beans(Converter::class.java, ConfigurationPropertiesBinding.VALUE, applicationContext.getBeanFactory())

        /**
         * GenericConverters
         */
        private val genericConverters = beans(
            GenericConverter::class.java,
            ConfigurationPropertiesBinding.VALUE,
            applicationContext.getBeanFactory()
        )


        /**
         * 从Spring BeanFactory当中, 找到所有的qualifier和type匹配的对应类型的Bean的列表
         *
         * @param type beanType
         * @param qualifier qualifier
         * @param beanFactory beanFactory
         * @return 根据Qualifier和beanType, 从BeanFactory当中去寻找到的合适的Bean的列表
         */
        private fun <T : Any> beans(type: Class<T>, qualifier: String, beanFactory: ListableBeanFactory): List<T> {
            return qualifiedBeansOfType(beanFactory, type, qualifier).values.toList()
        }

        /**
         * 检查是否存在有从BeanFactory当中去搜索到合适的Converter?
         *
         * @return 如果从BeanFactory当中存在有合适的Converter, 那么return true; 否则return false
         */
        fun isEmpty(): Boolean = this.converters.isEmpty() && this.genericConverters.isEmpty()

        /**
         * 将从BeanFactory当中收集得到的所有的Converter全部都加入到[ConverterRegistry]当中去
         *
         * @param registry ConverterRegistry
         */
        fun addTo(registry: ConverterRegistry) {
            converters.forEach(registry::addConverter)
            genericConverters.forEach(registry::addConverter)
        }
    }
}