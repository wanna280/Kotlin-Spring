package com.wanna.boot.test.context

import com.wanna.boot.SpringBootConfiguration
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.test.context.ContextLoader
import com.wanna.framework.test.context.MergedContextConfiguration
import com.wanna.framework.test.context.TestContextBootstrapper
import com.wanna.framework.test.context.support.DefaultTestContextBootstrapper
import org.slf4j.LoggerFactory

/**
 * SpringBoot的[TestContextBootstrapper]实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
open class SpringBootTestContextBootstrapper : DefaultTestContextBootstrapper() {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(SpringBootTestContextBootstrapper::class.java)
    }

    /**
     * 获取默认的[ContextLoader]，我们使用[SpringBootContextLoader]去作为[ContextLoader]
     *
     * @param testClass testClass
     * @return SpringBootContextLoader
     */
    override fun getDefaultContextLoaderClass(testClass: Class<*>): Class<out ContextLoader> {
        return SpringBootContextLoader::class.java
    }

    /**
     * 对[MergedContextConfiguration]去进行更多的自定义, 我们这里需要根据WebEnvironment的类型去构建出来不同类型的[MergedContextConfiguration]
     *
     * @param mergedContextConfiguration MergedContextConfiguration
     * @return 根据应用类型去得到的新的MergeContextConfiguration
     */
    override fun processMergedContextConfiguration(mergedContextConfiguration: MergedContextConfiguration): MergedContextConfiguration {
        val configurationClasses = getOrFindConfigurationClasses(mergedContextConfiguration)
        val propertySourceProperties = mergedContextConfiguration.getPropertySourceProperties()
        val mergedConfig =
            createModifiedConfig(mergedContextConfiguration, configurationClasses, propertySourceProperties)
        val webEnvironment = getWebEnvironment(mergedContextConfiguration.getTestClass())
        if (webEnvironment != null) {
            // TODO, 类型推断(目前先写死)
            return MvcMergedContextConfiguration(mergedConfig)
        }

        return mergedConfig
    }

    /**
     * 获取/寻找候选的配置类
     *
     * @param mergedConfig MergedContextConfiguration
     * @return 配置类列表
     */
    protected open fun getOrFindConfigurationClasses(mergedConfig: MergedContextConfiguration): Array<Class<*>> {
        val classes = mergedConfig.getClasses()

        // 如果给定的全部类当中都标注了@TestConfiguration，并且没有locations的话
        // 默认情况下，因为classes为空，因此会进入到这个if代码块当中...
        if (!containsNonTestComponent(classes) && mergedConfig.getLocations().isEmpty()) {
            // 那么从testClass所在的包上去检查@SpringBootConfiguration
            val clazz = AnnotatedClassFinder(SpringBootConfiguration::class.java)
                .findFromClass(mergedConfig.getTestClass())
            if (clazz != null) {
                if (logger.isInfoEnabled) {
                    logger.info("寻找到标注了@SpringBootConfiguration的类为[${clazz.name}], 将会使用它去进行测试")
                }
                return classes + arrayOf(clazz)
            } else {
                throw IllegalStateException("无法通过@SpringBootConfiguration去寻找到合适的配置类, 你需要使用@ContextConfiguration或者是@SpringBootTest(classes=...)去进行指定")
            }
        }
        return classes
    }

    /**
     * 检查给定的这些配置类当中是否有不是[TestConfiguration]的类？
     *
     * @param classes 待检查的配置类列表
     * @return 如果存在未标注[TestConfiguration]的类，那么return true；如果给定的这些类全部都标注了[TestConfiguration]注解的话，return false
     */
    private fun containsNonTestComponent(classes: Array<Class<*>>): Boolean {
        classes.forEach {
            if (AnnotatedElementUtils.getMergedAnnotation(it, TestConfiguration::class.java) == null) {
                return true
            }
        }
        return false
    }

    /**
     * 从testClass上的[SpringBootTest]注解上找到[SpringBootTest.WebEnvironment]
     *
     * @param testClass testClass
     * @return WebEnvironment
     */
    protected open fun getWebEnvironment(testClass: Class<*>): SpringBootTest.WebEnvironment? {
        val annotation = AnnotatedElementUtils.getMergedAnnotation(testClass, SpringBootTest::class.java)
        return annotation?.webEnvironment
    }

    /**
     * 创建一个修改的[MergedContextConfiguration]
     */
    protected open fun createModifiedConfig(
        mergedConfig: MergedContextConfiguration,
        classes: Array<Class<*>>,
        propertySourceProperties: Array<String>
    ): MergedContextConfiguration {
        return MergedContextConfiguration(
            mergedConfig.getTestClass(),
            mergedConfig.getContextLoader(),
            mergedConfig.getLocations(),
            classes,
            mergedConfig.getInitializers(),
            mergedConfig.getPropertySourceLocations(),
            propertySourceProperties,
            mergedConfig.getActiveProfiles(),
            mergedConfig.getCacheAwareContextLoaderDelegate()
        )
    }
}