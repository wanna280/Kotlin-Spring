package com.wanna.boot.autoconfigure.admin

import com.wanna.boot.admin.SpringApplicationAdminMXBeanRegistrar
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.environment.Environment

/**
 * SpringBoot Admin的JMX自动配置类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/17
 *
 * @see SpringApplicationAdminMXBeanRegistrar
 */
@Configuration(proxyBeanMethods = false)
open class SpringApplicationAdminJmxAutoConfiguration {

    companion object {

        /**
         * 自定义SpringBoot Admin的MXBean的属性值
         */
        private const val JMX_NAME_PROPERTY = "spring.application.admin.jmx-name"

        /**
         * 默认是SpringBoot Admin的MXBean的ObjectName
         */
        private const val DEFAULT_JMX_NAME = "org.springframework.boot:type=Admin,name=SpringApplication"
    }

    /**
     * 给Spring BeanFactory当中导入一个[SpringApplicationAdminMXBeanRegistrar], 负责去暴露SpringBoot Admin的MXBean
     *
     * @param environment environment
     */
    @Bean
    open fun springApplicationAdminMXBeanRegistrar(environment: Environment): SpringApplicationAdminMXBeanRegistrar {
        val objectName = environment.getProperty(JMX_NAME_PROPERTY, DEFAULT_JMX_NAME)
        return SpringApplicationAdminMXBeanRegistrar(objectName)
    }

}