package com.wanna.framework.test.context.support

import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.lang.Nullable
import com.wanna.framework.test.context.*
import com.wanna.framework.test.context.support.ContextLoaderUtils.resolveContextConfigurationAttributes
import com.wanna.framework.util.BeanUtils
import org.slf4j.LoggerFactory

/**
 * [TestContextBootstrapper]的抽象模板实现，负责解析testClass上的相关注解，去最终构建出来[TestContext]；
 * 为所有的具体的实现类提供基础的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 *
 * @see DefaultTestContextBootstrapper
 */
abstract class AbstractTestContextBootstrapper : TestContextBootstrapper {
    /**
     * Logger
     */
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * BootstrapContext
     */
    private var bootstrapContext: BootstrapContext? = null

    override fun setBootstrapContext(bootstrapContext: BootstrapContext) {
        this.bootstrapContext = bootstrapContext
    }

    override fun getBootstrapContext() = bootstrapContext ?: throw IllegalStateException("BootstrapContext还未完成初始化")

    /**
     * 构建出来[TestContext]，把testClass、MergedContextConfiguration、CacheAwareContextLoaderDelegate
     * 全部都组合到[DefaultTestContext]当中去进行返回
     *
     * @return TestContext(组合了testClass、MergedContextConfiguration、CacheAwareContextLoaderDelegate)
     */
    override fun buildTestContext(): TestContext =
        DefaultTestContext(
            getBootstrapContext().getTestClass(),
            buildMergedContextConfiguration(),
            getBootstrapContext().getCacheAwareContextLoaderDelegate()
        )

    /**
     * 解析testClass上的相关注解，从而去构建出来[MergedContextConfiguration]
     *
     * @return 构建出来的MergedContextConfiguration
     */
    override fun buildMergedContextConfiguration(): MergedContextConfiguration {
        val testClass = getBootstrapContext().getTestClass()
        val cacheAwareContextLoaderDelegate = getBootstrapContext().getCacheAwareContextLoaderDelegate()

        // 解析testClass上所有直接/间接标注的@ContextConfiguration注解
        AnnotatedElementUtils.getMergedAnnotation(testClass, ContextConfiguration::class.java)
            ?: return buildDefaultMergedContextConfiguration(testClass, cacheAwareContextLoaderDelegate)

        // 根据testClass上的@ContextConfiguration注解去构建出来MergedContextConfiguration
        return buildMergedContextConfiguration(
            testClass,
            resolveContextConfigurationAttributes(testClass),
            cacheAwareContextLoaderDelegate
        )
    }

    /**
     * 根据给定的[ContextConfigurationAttributes]列表，去Merge得到一个[MergedContextConfiguration]
     *
     * @param testClass testClass
     * @param attributesList @ContextConfiguration注解的属性列表
     * @param cacheAwareContextLoaderDelegate CacheAwareContextLoaderDelegate
     * @return MergedContextConfiguration
     */
    private fun buildMergedContextConfiguration(
        testClass: Class<*>,
        attributesList: List<ContextConfigurationAttributes>,
        cacheAwareContextLoaderDelegate: CacheAwareContextLoaderDelegate
    ): MergedContextConfiguration {

        // 解析得到ContextLoader
        val contextLoader = resolveContextLoader(testClass, attributesList)

        val classes = ArrayList<Class<*>>()
        val initializers = ArrayList<Class<out ApplicationContextInitializer<*>>>()
        val locations = ArrayList<String>()

        // 根据所有的ContextConfigurationAttributes去Merge所有的classes/locations/initializers
        attributesList.forEach {
            if (contextLoader is SmartContextLoader) {
                // 对于SmartContextLoader来说，可以去处理classes和locations
                contextLoader.processContextConfiguration(it)
                classes += it.classes
                locations += it.locations
            } else {
                // 非SmartContextLoader，无需处理classes
                locations += contextLoader.processLocations(*it.locations)
            }
            initializers + it.initializers
        }

        // 解析@TestPropertySource，得到测试时需要去进行使用的PropertySource
        val testPropertySources = TestPropertySourceUtils.buildMergedTestPropertySources(testClass)

        // 解析@ActiveProfiles注解，得到测试时需要去进行使用的profile
        val activeProfiles = ActiveProfilesUtils.resolveActiveProfiles(testClass)

        // Merge各个ContextConfigurationAttributes以及更多的配置信息，去得到MergedContextConfiguration
        val mergedContextConfiguration = MergedContextConfiguration(
            testClass,
            contextLoader,
            locations.toTypedArray(),
            classes.toTypedArray(),
            initializers.toTypedArray(),
            testPropertySources.getLocations(),
            testPropertySources.getProperties(),
            activeProfiles,
            cacheAwareContextLoaderDelegate
        )

        // 将MergedContextConfiguration交给子类去进行后置处理
        return processMergedContextConfiguration(mergedContextConfiguration)
    }


    /**
     * 给子类一个机会，对于[MergedContextConfiguration]去进行自定义
     *
     * @param mergedContextConfiguration MergedContextConfiguration
     * @return 处理之后的MergedContextConfigurations
     */
    protected open fun processMergedContextConfiguration(mergedContextConfiguration: MergedContextConfiguration): MergedContextConfiguration {
        return mergedContextConfiguration
    }

    /**
     * 根据testClass和[ContextConfigurationAttributes]列表解析得到合适的[ContextLoader]
     *
     * @param testClass testClass
     * @param configAttributes @ContextConfiguration注解的属性信息
     */
    protected open fun resolveContextLoader(
        testClass: Class<*>,
        configAttributes: List<ContextConfigurationAttributes>
    ): ContextLoader {

        // 解析明确在@ContextConfiguration注解上给出来的ContextLoaderClass
        var loaderClass = resolveExplicitContextLoaderClass(configAttributes)

        // 如果没有解析出来的话，那么交给子类去决定出来一个默认的ContextLoader
        if (loaderClass == null) {
            loaderClass = getDefaultContextLoaderClass(testClass)
        }
        if (logger.isTraceEnabled) {
            logger.trace("将会使用[$loaderClass]去作为Test的ContextLoader去进行加载Spring ApplicationContext")
        }
        // 实例化ContextLoader
        return BeanUtils.instantiateClass(loaderClass, ContextLoader::class.java)
    }

    /**
     * 从[ContextConfigurationAttributes]当中去解析出来一个合适的[ContextLoader]的类
     *
     * @param configAttributes @ContextConfiguration注解的属性信息
     * @return 解析得到的ContextLoader Class(如果没有解析到的话，return null)
     */
    @Nullable
    protected open fun resolveExplicitContextLoaderClass(configAttributes: List<ContextConfigurationAttributes>): Class<out ContextLoader>? {
        configAttributes.forEach {
            if (it.contextLoaderClass != ContextLoader::class.java) {
                return it.contextLoaderClass
            }
        }
        return null
    }

    /**
     * 模板方法，交给子类去决定出来默认的[ContextLoader]的Class
     *
     * @param testClass testClass
     * @return 解析得到的ContextLoader Class，不能为null
     */
    protected abstract fun getDefaultContextLoaderClass(testClass: Class<*>): Class<out ContextLoader>

    /**
     * 构建一个默认的[MergedContextConfiguration]
     *
     * @param testClass testClass
     * @param cacheAwareContextLoaderDelegate CacheAwareContextLoaderDelegate
     */
    private fun buildDefaultMergedContextConfiguration(
        testClass: Class<*>,
        cacheAwareContextLoaderDelegate: CacheAwareContextLoaderDelegate
    ): MergedContextConfiguration {
        val configurationAttributes = listOf(ContextConfigurationAttributes(testClass))
        return buildMergedContextConfiguration(testClass, configurationAttributes, cacheAwareContextLoaderDelegate)
    }

    override fun getTestExecutionListeners(): List<TestExecutionListener> {
        return listOf(DependencyInjectionTestExecutionListener())
    }
}