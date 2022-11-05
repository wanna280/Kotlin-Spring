package com.wanna.boot.test.context

import com.wanna.boot.ApplicationType
import com.wanna.boot.SpringApplication
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.test.context.ContextConfigurationAttributes
import com.wanna.framework.test.context.ContextLoader
import com.wanna.framework.test.context.MergedContextConfiguration
import com.wanna.framework.test.context.SmartContextLoader
import com.wanna.framework.util.BeanUtils

/**
 * SpringBoot的[ContextLoader]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
open class SpringBootContextLoader : SmartContextLoader {

    override fun processLocations(vararg locations: String): Array<String> {
        return arrayOf(*locations)
    }

    override fun processContextConfiguration(configAttributes: ContextConfigurationAttributes) {

    }

    override fun loadContext(vararg locations: String): ApplicationContext {
        throw UnsupportedOperationException("不支持使用SpringBootContextLoader的loadContext(String...)方法去加载ApplicationContext")
    }

    /**
     * 加载SpringBoot的[ApplicationContext], 构建出来[SpringApplication]并完成启动并得到[ApplicationContext]
     *
     * @param mergedContextConfiguration MergedContextConfiguration
     * @return 加载到的[SpringApplication]的[ApplicationContext]
     */
    override fun loadContext(mergedContextConfiguration: MergedContextConfiguration): ApplicationContext {
        val locations = mergedContextConfiguration.getLocations()
        val configClasses = mergedContextConfiguration.getClasses()

        // 获取到SpringApplication
        val application = getSpringApplication()

        // 初始化mainClass、sources、primarySources
        application.setMainApplicationClass(mergedContextConfiguration.getTestClass())
        application.addPrimarySources(*configClasses)
        application.addSources(*locations)

        // 初始化ApplicationInitializers
        val initializers = getInitializers(mergedContextConfiguration, application)
        application.setInitializers(initializers)

        // 初始化Web ApplicationType
        if (mergedContextConfiguration is MvcMergedContextConfiguration) {
            application.setApplicationType(ApplicationType.MVC)
        }

        return application.run()
    }

    /**
     * 获取[ApplicationContextInitializer]列表
     *
     * @param config MergedContextConfiguration
     * @param application SpringApplication
     */
    protected open fun getInitializers(
        config: MergedContextConfiguration,
        application: SpringApplication
    ): List<ApplicationContextInitializer<*>> {
        val initializers = config.getInitializers()
        return initializers.map { BeanUtils.instantiateClass(it) }
    }

    /**
     * 获取到[SpringApplication]
     *
     * @return SpringApplication
     */
    protected open fun getSpringApplication(): SpringApplication {
        return SpringApplication()
    }
}