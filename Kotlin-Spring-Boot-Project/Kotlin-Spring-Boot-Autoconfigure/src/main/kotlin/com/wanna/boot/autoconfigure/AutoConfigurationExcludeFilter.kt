package com.wanna.boot.autoconfigure

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.core.type.filter.TypeFilter

/**
 * 自动配置包的排除过滤器，避免ComponentScan过程当中扫描到了自动配置的类
 */
open class AutoConfigurationExcludeFilter : TypeFilter {

    // 缓存的自动配置类列表
    private var configurations: List<String>? = null

    /**
     * 判断它是否既是一个@Configuration配置类，也是一个AutoConfiguration配置类
     *
     * @param clazz 要去进行匹配的配置类
     * @return 如果又是@Configuration配置类，又是AutoConfiguration，return true，否则return false
     */
    override fun matches(clazz: Class<*>?) = clazz != null && isConfiguration(clazz) && isAutoConfiguration(clazz)

    /**
     * 判断给定的类是否是一个配置类
     *
     * @param clazz 要去匹配的目标类
     * @return 如果它标注了@Configuration，那么return true，否则return false
     */
    open fun isConfiguration(clazz: Class<*>): Boolean =
        AnnotatedElementUtils.isAnnotated(clazz, Configuration::class.java)

    /**
     * 判断给定的类是否是一个自动配置类
     *
     * @param clazz 要去进行匹配的目标类
     * @return 它是否配置在了SpringFactories当中？如果存在的话，return true，否则return false
     */
    open fun isAutoConfiguration(clazz: Class<*>) = getAutoConfigurations().contains(clazz.name)

    /**
     * 从SpringFactories当中去获取所有的自动配置类的列表
     *
     * @return 自动配置类列表
     */
    private fun getAutoConfigurations(): List<String> {
        if (this.configurations == null) {
            this.configurations = SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration::class.java)
        }
        return this.configurations!!
    }
}