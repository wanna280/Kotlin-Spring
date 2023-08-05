package com.wanna.boot.context.config

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextException
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.beans.BeanUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import kotlin.jvm.Throws

/**
 * 当SpringBoot对[ApplicationContext]去进行初始化时, 自动拿出来所有的"context.initializer.classes"([PROPERTY_NAME])
 * 当中去配置的[ApplicationContextInitializer]的类, 去对[ApplicationContext]完成初始化工作
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/11
 */
open class DelegatingApplicationContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext>,
    Ordered {
    companion object {
        /**
         * 通过配置文件的方式, 去配置一些类, 让这些类可以被自动扫描, 当做[ApplicationContextInitializer]去进行执行
         */
        private const val PROPERTY_NAME = "context.initializer.classes"
    }


    /**
     * Order
     */
    private var order: Int = 0

    /**
     * 根据"context.initializer.classes"这个配置当中配置的[ApplicationContextInitializer],
     * 从而去实现对[ApplicationContext]的相关初始化工作
     *
     * @param applicationContext ApplicationContext
     */
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        // 从Environment当中, 根据"context.initializer.classes"去获取到所有配置的Initializer的Class
        val initializerClasses = getInitializerClasses(applicationContext.getEnvironment())

        if (initializerClasses.isNotEmpty()) {
            applyInitializerClasses(applicationContext, initializerClasses)
        }
    }

    /**
     * 将配置的所有的[ApplicationContextInitializer]去应用给[ApplicationContext]
     *
     * @param applicationContext ApplicationContext
     * @param initializerClasses 探测到的ApplicationContextInitializer的类
     */
    @Suppress("UNCHECKED_CAST")
    private fun applyInitializerClasses(
        applicationContext: ConfigurableApplicationContext,
        initializerClasses: List<Class<ApplicationContextInitializer<*>>>
    ) {
        for (initializerClass in initializerClasses) {
            val initializer = instantiateInitializer(initializerClass)
            (initializer as ApplicationContextInitializer<ConfigurableApplicationContext>).initialize(applicationContext)
        }
    }

    private fun instantiateInitializer(initializer: Class<ApplicationContextInitializer<*>>): ApplicationContextInitializer<*> {
        try {
            return BeanUtils.instantiateClass(initializer)
        } catch (ex: Exception) {
            throw ApplicationContextException(
                "Failed to instantiate context initializer class [${initializer.name}]",
                ex
            )
        }
    }

    /**
     * 从[ConfigurableEnvironment]当中去探测到所有的通过"context.initializer.classes"去配置的类
     *
     * @param environment Environment
     * @return 探测到的ApplicationContextInitializer的类的列表
     */
    private fun getInitializerClasses(environment: ConfigurableEnvironment): List<Class<ApplicationContextInitializer<*>>> {
        val listenerClasses = ArrayList<Class<ApplicationContextInitializer<*>>>()
        val classNames = environment.getProperty(PROPERTY_NAME)
        if (StringUtils.hasText(classNames)) {
            val initializerClassNames = StringUtils.commaDelimitedListToStringArray(classNames)
            for (className in initializerClassNames) {
                listenerClasses += getInitializerClass(className)
            }
        }
        return listenerClasses
    }

    /**
     * 根据className去获取到[ApplicationContextInitializer]
     *
     * @param className className
     * @return ApplicationContextInitializer的类
     * @throws IllegalStateException 如果配置的类的类型不是[ApplicationContextInitializer]
     * @throws ApplicationContextException 如果该类无法被加载到的话
     */
    @Throws(ApplicationContextException::class, IllegalStateException::class)
    private fun getInitializerClass(className: String): Class<ApplicationContextInitializer<*>> {
        try {
            val clazz =
                ClassUtils.forName<ApplicationContextInitializer<*>>(className, ClassUtils.getDefaultClassLoader())
            if (!ClassUtils.isAssignFrom(ApplicationContextInitializer::class.java, clazz)) {
                throw IllegalStateException("class [$className] must implement ApplicationContextInitializer")
            }
            return clazz
        } catch (ex: ClassNotFoundException) {
            throw ApplicationContextException("Failed to load context initializer class [$className]")
        }
    }


    override fun getOrder(): Int = this.order

    open fun setOrder(order: Int) {
        this.order = order
    }
}