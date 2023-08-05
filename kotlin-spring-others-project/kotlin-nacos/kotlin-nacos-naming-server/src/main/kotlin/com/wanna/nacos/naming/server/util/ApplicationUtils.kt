package com.wanna.nacos.naming.server.util

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * [ApplicationContext]的工具类, 它实现了[ApplicationContextInitializer];
 * 在Spring的[ApplicationContext]创建完成时, 会自动保存下来[ApplicationContext], 供外部作为工具类去进行使用
 *
 * @see ApplicationContextInitializer
 */
open class ApplicationUtils : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {
        /**
         * ApplicationContext
         */
        @JvmStatic
        private var applicationContext: ApplicationContext? = null

        /**
         * 获取到正在运行当中的ApplicationContext
         */
        @JvmStatic
        fun getApplicationContext(): ApplicationContext =
            applicationContext ?: throw IllegalStateException("ApplicationContext还未完成初始化")

        /**
         * 从ApplicationContext当中去进行getBean
         *
         * @param beanName beanName
         * @return 从ApplicationContext当中去获取到的Bean
         */
        @JvmStatic
        fun getBean(beanName: String): Any = getApplicationContext().getBean(beanName)

        /**
         * 从ApplicationContext当中根据beanName和type去进行getBean
         *
         * @param beanName beanName
         * @param type type
         */
        @JvmStatic
        fun <T : Any> getBean(beanName: String, type: Class<T>): T = getApplicationContext().getBean(beanName, type)
    }

    /**
     * 初始化ApplicationContext的Callback, 在它初始化时, 我们将ApplicationContext去进行保存下来
     *
     * @param applicationContext ApplicationContext
     */
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        ApplicationUtils.applicationContext = applicationContext
    }
}