package com.wanna.framework.test.context

import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.test.context.cache.DefaultCacheAwareContextLoaderDelegate
import com.wanna.framework.test.context.support.DefaultBootstrapContext
import com.wanna.framework.test.context.support.DefaultTestContextBootstrapper
import com.wanna.framework.test.context.web.WebAppConfiguration
import com.wanna.framework.test.context.web.WebTestContextBootstrapper
import com.wanna.framework.beans.BeanUtils
import com.wanna.common.logging.LoggerFactory

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
object BootstrapUtils {

    /**
     * Logger
     */
    @JvmStatic
    private val logger = LoggerFactory.getLogger(BootstrapUtils::class.java)

    /**
     * 根据testClass去创建出来[BootstrapContext]
     *
     * @param testClass testClass
     * @return BootstrapContext
     */
    @JvmStatic
    fun createBootstrapContext(testClass: Class<*>): BootstrapContext {
        val cacheAwareContextLoaderDelegate = createCacheAwareContextLoaderDelegate()
        return DefaultBootstrapContext(testClass, cacheAwareContextLoaderDelegate)
    }

    /**
     * 根据[BootstrapContext]的testClass去解析出来合适的[TestContextBootstrapper];
     * 检查testClass上是否有标注[BootstrapWith]注解, 如果有的话, 那么使用该[TestContextBootstrapper]去进行构建TestContext
     *
     * @param bootstrapContext BootstrapContext
     * @return TestContextBootstrapper(内部组合了BootstrapContext)
     */
    @JvmStatic
    fun resolveTestContextBootstrapper(bootstrapContext: BootstrapContext): TestContextBootstrapper {
        val testClass = bootstrapContext.getTestClass()

        // 首先, 从类上去找到@BootstrapWith注解, 从其中提取出来TestContextBootstrapper
        // 如果类上没有@BootstrapWith注解, 那么检查是否有@WebAppConfiguration注解去选择出来合适的TestContextBootstrapper
        val clazz = resolveExplicitTestContextBootstrapper(testClass)
            ?: resolveDefaultTestContextBootstrapper(testClass)

        // 创建出来TestContextBootstrapper, 并将BootstrapContext设置进去
        val bootstrapper = BeanUtils.instantiateClass(clazz, TestContextBootstrapper::class.java)
        bootstrapper.setBootstrapContext(bootstrapContext)
        return bootstrapper
    }

    /**
     * 从给定的testClass上去找到[BootstrapWith]注解, 从而找到需要使用的[TestContextBootstrapper]
     *
     * @param testClass testClass
     * @return 从testClass上找到的@BootstrapWith注解当中配置的value属性
     */
    @JvmStatic
    private fun resolveExplicitTestContextBootstrapper(testClass: Class<*>): Class<*>? {
        val annotations = AnnotatedElementUtils.getAllMergedAnnotations(testClass, BootstrapWith::class.java)
        if (annotations.isEmpty()) {
            return null
        }
        if (annotations.size > 1) {
            throw IllegalStateException("从[${testClass.name}]类上找到了多个[${BootstrapWith::class.java}]注解, [$annotations]")
        }
        return annotations.iterator().next().value.java
    }

    /**
     * 根据testClass去找到默认的[TestContextBootstrapper].
     *
     * 如果testClass上有[WebAppConfiguration]注解的话, 创建一个[WebTestContextBootstrapper];
     * 如果testClass上没有[WebAppConfiguration]注解的话, 创建一个[DefaultTestContextBootstrapper]
     *
     * @param testClass testClass
     * @return TestContextBootstrapper Class
     */
    @JvmStatic
    private fun resolveDefaultTestContextBootstrapper(testClass: Class<*>): Class<*> {
        val webAppConfiguration = AnnotatedElementUtils.getMergedAnnotation(testClass, WebAppConfiguration::class.java)
        return if (webAppConfiguration != null) WebTestContextBootstrapper::class.java else DefaultTestContextBootstrapper::class.java
    }

    /**
     * 创建出来[CacheAwareContextLoaderDelegate]
     *
     * @return CacheAwareContextLoaderDelegate
     */
    @JvmStatic
    private fun createCacheAwareContextLoaderDelegate(): CacheAwareContextLoaderDelegate {
        return DefaultCacheAwareContextLoaderDelegate()
    }
}