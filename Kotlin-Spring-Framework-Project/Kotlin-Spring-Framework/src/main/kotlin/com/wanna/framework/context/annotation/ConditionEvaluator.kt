package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.annotation.ConfigurationCondition.ConfigurationPhase
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.EnvironmentCapable
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.core.type.AnnotatedTypeMetadata
import com.wanna.framework.core.util.ClassUtils

/**
 * 这是一个条件计算器，计算某个Bean是否应该被导入到容器当中？
 *
 * @see com.wanna.framework.context.util.ConfigurationClassParser.conditionEvaluator
 * @see com.wanna.framework.context.util.ConfigurationClassParser.processConfigurationClass
 */
open class ConditionEvaluator(
    private val registry: BeanDefinitionRegistry,
    private val environment: Environment,
    private val resourceLoader: ResourceLoader
) {

    // ConditionContext，维护beanDefinitionRegistry以及environment等
    private val context: ConditionContext = ConditionContextImpl(registry, environment, resourceLoader)

    /**
     * 根据注解信息去判断，是否应该跳过？
     *
     * @param metadata 方法/类的注解信息的描述
     */
    open fun shouldSkip(metadata: AnnotatedTypeMetadata?): Boolean {
        return shouldSkip(metadata, null)
    }


    /**
     * @param metadata 类/方法当中的注解信息的描述
     * @param phase 当前处于哪个阶段？是解析配置类还是注册Bean？
     */
    open fun shouldSkip(metadata: AnnotatedTypeMetadata?, phase: ConfigurationPhase?): Boolean {
        // 如果没有给定metadata，或者metadata当中没有标注@Conditional注解，那么肯定不应该跳过，return false
        if (metadata == null || !metadata.isAnnotated(Conditional::class.java.name)) {
            return false
        }

        // 实例化@Conditional注解的value属性当中配置的Condition，并使用比较器去完成Conditions的排序
        val conditions = getConditionClasses(metadata)
            .map { getCondition(it, this.context.getClassLoader()) }
            .toMutableList()
        AnnotationAwareOrderComparator.sort(conditions)

        // 遍历所有的Condition，去完成匹配
        conditions.forEach {
            // 拿到Condition对象需要进行匹配的阶段
            var requiredPhase: ConfigurationPhase? = null
            if (it is ConfigurationCondition) {
                requiredPhase = it.getConfigurationPhase()
            }
            // (1)如果requiredPhase==null，说明该Condition无论哪个阶段都需要去进行匹配，一定会调用matches
            // (2)如果requiredPhase==phase，说明阶段匹配了，需要去进行匹配；如果阶段不匹配(requiredPhase!=phase)，那么跳过
            if ((requiredPhase == null || requiredPhase == phase) && !it.matches(this.context, metadata)) {
                return true
            }
        }

        return false
    }

    /**
     * 给定condition的类名，去完成Condition的实例化
     */
    private fun getCondition(conditionClassName: String, classLoader: ClassLoader?): Condition {
        val classLoaderToUse = classLoader ?: ClassUtils.getDefaultClassLoader()
        val conditionClass = ClassUtils.forName<Condition>(conditionClassName, classLoaderToUse)
        return ClassUtils.newInstance(conditionClass)
    }

    /**
     * 获取@Conditional注解当中标注的Condition的类的className列表
     */
    @Suppress("UNCHECKED_CAST")
    private fun getConditionClasses(metadata: AnnotatedTypeMetadata): Array<String> {
        return (metadata.getAnnotationAttributes(Conditional::class.java)["value"] as Array<Class<*>>)
            .map { it.name }
            .toTypedArray()
    }


    /**
     * 这是一个ConditionContext的具体实现，主要维护beanDefinitionRegistry等环境信息
     */
    private class ConditionContextImpl(
        private val registry: BeanDefinitionRegistry,
        private val environment: Environment?,
        private val resourceLoader: ResourceLoader
    ) : ConditionContext {

        private val beanFactory: ConfigurableListableBeanFactory? = deduceBeanFactory(registry)

        private var classLoader: ClassLoader = deduceClassLoader(this.beanFactory)

        override fun getRegistry(): BeanDefinitionRegistry = this.registry

        override fun getBeanFactory(): ConfigurableListableBeanFactory? = this.beanFactory

        override fun getEnvironment(): Environment {
            return deduceEnvironment(this.registry)
        }

        override fun getClassLoader(): ClassLoader {
            return this.classLoader
        }

        /**
         * 从beanFactory当中腿短ClassLoader
         */
        private fun deduceClassLoader(beanFactory: ConfigurableListableBeanFactory?): ClassLoader {
            if (beanFactory != null) {
                this.classLoader = beanFactory.getBeanClassLoader()
            }
            return ClassUtils.getDefaultClassLoader()
        }

        /**
         * 推断环境
         */
        private fun deduceEnvironment(registry: BeanDefinitionRegistry): Environment {
            if (this.environment != null) {
                return this.environment
            }
            if (registry is EnvironmentCapable) {
                return registry.getEnvironment()
            }
            return StandardEnvironment()
        }

        /**
         * 推断beanFactory
         */
        private fun deduceBeanFactory(registry: BeanDefinitionRegistry): ConfigurableListableBeanFactory? {
            if (registry is ConfigurableListableBeanFactory) {
                return registry
            }
            if (registry is ConfigurableApplicationContext) {
                return registry.getBeanFactory()
            }
            return null
        }

        override fun getResourceLoader() = this.resourceLoader
    }
}