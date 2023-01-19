package com.wanna.framework.test.context.support

import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.lang.Nullable
import com.wanna.framework.test.context.*
import com.wanna.framework.test.context.support.ContextLoaderUtils.resolveContextConfigurationAttributes
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.common.logging.LoggerFactory

/**
 * [TestContextBootstrapper]的抽象模板实现, 负责解析testClass上的相关注解, 去最终构建出来[TestContext];
 * 为所有的[TestContextBootstrapper]的具体实现类提供基础的实现
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

    /**
     * 设置[BootstrapContext]
     *
     * @param bootstrapContext BootstrapContext
     */
    override fun setBootstrapContext(bootstrapContext: BootstrapContext) {
        this.bootstrapContext = bootstrapContext
    }

    /**
     * 获取BootstrapContext
     *
     * @return BootstrapContext
     * @throws IllegalStateException 如果还没完成Bootstrap的初始化
     */
    @Throws(IllegalStateException::class)
    override fun getBootstrapContext() = bootstrapContext ?: throw IllegalStateException("BootstrapContext还未完成初始化")

    /**
     * 构建出来[TestContext], 把testClass、MergedContextConfiguration、CacheAwareContextLoaderDelegate
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
     * 解析testClass上的相关注解, 从而去构建出来[MergedContextConfiguration]
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
     * 根据给定的[ContextConfigurationAttributes]列表, 去Merge得到一个[MergedContextConfiguration]
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
                // 对于SmartContextLoader来说, 可以去处理classes和locations
                contextLoader.processContextConfiguration(it)
                classes += it.classes
                locations += it.locations
            } else {
                // 非SmartContextLoader, 无需处理classes
                locations += contextLoader.processLocations(*it.locations)
            }
            initializers + it.initializers
        }

        // 解析@TestPropertySource, 得到测试时需要去进行使用的PropertySource
        val testPropertySources = TestPropertySourceUtils.buildMergedTestPropertySources(testClass)

        // 解析@ActiveProfiles注解, 得到测试时需要去进行使用的profile
        val activeProfiles = ActiveProfilesUtils.resolveActiveProfiles(testClass)

        // Merge各个ContextConfigurationAttributes以及更多的配置信息, 去得到MergedContextConfiguration
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
     * 给子类一个机会, 对于[MergedContextConfiguration]去进行自定义
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

        // 如果没有解析出来的话, 那么交给子类去决定出来一个默认的ContextLoader
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
     * @return 解析得到的ContextLoader Class(如果没有解析到的话, return null)
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
     * 模板方法, 交给子类去决定出来默认的[ContextLoader]的Class
     *
     * @param testClass testClass
     * @return 解析得到的ContextLoader Class, 不能为null
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

    /**
     * 获取监听一个Test任务的[TestExecutionListener]列表, 对Test任务的各个生命周期去进行自定义
     *
     * @return [TestExecutionListener]列表
     */
    final override fun getTestExecutionListeners(): List<TestExecutionListener> {
        val testClass = getBootstrapContext().getTestClass()
        val listenersAnnotation =
            AnnotatedElementUtils.getMergedAnnotation(testClass, TestExecutionListeners::class.java)
        val listenerClasses: Collection<Class<out TestExecutionListener>>

        // 是否需要引用默认的Listener?
        var useDefaults = false
        // 如果没有@TestExecutionListeners注解的话, 将会采用默认的TestExecutionListener
        if (listenersAnnotation == null) {
            listenerClasses = getDefaultTestExecutionListeners()
            useDefaults = true
            // 如果有有@TestExecutionListeners注解的话, 那么将会采用给定的TestExecutionListener
        } else {
            listenerClasses = (listenersAnnotation.listeners + listenersAnnotation.value).map { it.java }
        }

        // 对于给定的TestExecutionListener去进行实例化
        val listeners = ArrayList(instantiateListeners(listenerClasses))

        // 如果应用默认的Listener的话, 将会被自动排序
        if (useDefaults) {
            AnnotationAwareOrderComparator.sort(listeners)
        }
        return listeners
    }


    /**
     * 根据给定的所有的[TestExecutionListener]类, 尝试去实例化所有的[TestExecutionListener]
     *
     * @param listenerClasses TestExecutionListener Classes
     * @return 实例化完成的[TestExecutionListener]列表
     * @throws Throwable 如果实例化对象过程中出现异常的话
     */
    @Throws(Throwable::class)
    private fun instantiateListeners(listenerClasses: Collection<Class<out TestExecutionListener>>): List<TestExecutionListener> {
        val listeners = ArrayList<TestExecutionListener>()
        listenerClasses.forEach {
            try {
                listeners += BeanUtils.instantiateClass(it, TestExecutionListener::class.java)
            } catch (ex: Throwable) {
                if (ex !is NoClassDefFoundError) {
                    throw ex
                }
                if (logger.isDebugEnabled) {
                    logger.debug("无法解析到依赖[${it.name}], 将会跳过该TestExecutionListener的解析, 原因在于", ex.message)
                }
            }
        }
        return listeners
    }

    /**
     * 获取默认的[TestExecutionListener]列表
     *
     * @return 默认的TestExecutionListener列表
     */
    private fun getDefaultTestExecutionListeners(): Set<Class<out TestExecutionListener>> {
        val testExecutionListeners = LinkedHashSet<Class<out TestExecutionListener>>()
        val classloader = this::class.java.classLoader
        val listenerClassNames = getDefaultTestExecutionListenerClassNames()
        listenerClassNames.forEach {
            try {
                testExecutionListeners += ClassUtils.forName(it, classloader)
            } catch (ex: ClassNotFoundException) {
                if (logger.isDebugEnabled) {
                    logger.info("无法加载到TestExecutionListenr[$it]")
                }
            }
        }
        return testExecutionListeners
    }

    /**
     * 从SpringFactories当中去加载到默认的[TestExecutionListener]列表
     *
     * @return SpringFactories当中加载的[TestExecutionListener]列表
     */
    protected open fun getDefaultTestExecutionListenerClassNames(): List<String> {
        val factoryNames = SpringFactoriesLoader.loadFactoryNames(TestExecutionListener::class.java)
        if (logger.isInfoEnabled) {
            logger.info("从'META-INF/spring.factories'当中加载到的TestExecutionListener有下面这些：[$factoryNames]")
        }
        return ArrayList(factoryNames)
    }
}