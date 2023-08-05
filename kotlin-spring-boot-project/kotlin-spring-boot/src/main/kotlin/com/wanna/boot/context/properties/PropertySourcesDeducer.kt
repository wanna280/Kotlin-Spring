package com.wanna.boot.context.properties

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.support.PropertySourcesPlaceholderConfigurer
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.MutablePropertySources
import com.wanna.framework.core.environment.PropertySources
import com.wanna.framework.lang.Nullable
import com.wanna.common.logging.LoggerFactory
import kotlin.jvm.Throws

/**
 * [PropertySources]的Deducer的推断器工具类, 主要用于将[ApplicationContext]去推断成为[PropertySources]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/9
 *
 * @param applicationContext ApplicationContext
 */
class PropertySourcesDeducer(private val applicationContext: ApplicationContext) {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(PropertySourcesDeducer::class.java)
    }

    /**
     * 从[ApplicationContext]当中推断出来[PropertySources]
     *
     * @return 从ApplicationContext当中获取到的PropertySources
     * @throws IllegalStateException 如果无法提取到PropertySources
     */
    @Throws(IllegalStateException::class)
    fun getPropertySources(): PropertySources {
        // 1.尝试从ApplicationContext当中去获取到PropertySourcesPlaceholderConfigurer的PropertySource
        val placeholderConfigurer = getSinglePropertySourcesPlaceholderConfigurer()
        if (placeholderConfigurer != null) {
            return placeholderConfigurer.getAppliedPropertySources()
        }
        return extractEnvironmentPropertySources()
            ?: throw IllegalStateException("无法从PropertySourcesPlaceholderConfigurer/Environment当中去提取到PropertySources")
    }

    /**
     * 如果[ApplicationContext]只存在有一个[PropertySourcesPlaceholderConfigurer]的话,
     * 那么尝试从[ApplicationContext]当中去获取一个[PropertySourcesPlaceholderConfigurer]
     *
     * @return PropertySources Placeholder Configurer
     */
    @Nullable
    private fun getSinglePropertySourcesPlaceholderConfigurer(): PropertySourcesPlaceholderConfigurer? {
        val beans = applicationContext.getBeansForType(PropertySourcesPlaceholderConfigurer::class.java)
        if (beans.size == 1) {
            return beans.values.iterator().next();
        }
        if (beans.size > 1 && logger.isWarnEnabled) {
            logger.warn("从ApplicationContext当中找到了多个已经注册的PropertySourcesPlaceholderConfigurer, 将会采用默认的Environment去作为fallback")
        }
        return null
    }

    /**
     * 从[ApplicationContext]的[Environment]当中去提取出来环境的[PropertySources]
     *
     * @return Environment当中的[MutablePropertySources], 如果它不是[ConfigurableEnvironment]的话, return null
     */
    @Nullable
    private fun extractEnvironmentPropertySources(): MutablePropertySources? {
        val environment = this.applicationContext.getEnvironment()
        if (environment is ConfigurableEnvironment) {
            return environment.getPropertySources()
        }
        return null
    }
}