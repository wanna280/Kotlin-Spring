package com.wanna.framework.context.processor.factory.internal

import com.wanna.framework.beans.PropertyValues
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.context.annotation.ConfigurationClassUtils
import com.wanna.framework.context.ApplicationStartupAware
import com.wanna.framework.beans.factory.config.SingletonBeanRegistry
import com.wanna.framework.beans.factory.support.definition.AbstractBeanDefinition
import com.wanna.framework.context.annotation.*
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.context.processor.beans.InstantiationAwareBeanPostProcessor
import com.wanna.framework.context.processor.factory.BeanDefinitionRegistryPostProcessor
import com.wanna.framework.core.PriorityOrdered
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.core.util.AnnotationConfigUtils
import org.slf4j.LoggerFactory
import java.util.Optional

/**
 * 这是一个配置类处理器，用来扫描Spring当中的配置类，包括Configuration/Component/Bean等注解的处理
 */
open class ConfigurationClassPostProcessor : BeanDefinitionRegistryPostProcessor, PriorityOrdered, EnvironmentAware,
    BeanClassLoaderAware, ApplicationStartupAware {

    companion object {
        // ImportRegistry的beanName
        private val IMPORT_REGISTRY_BEAN_NAME = ConfigurationClassPostProcessor::class.java.name + ".importRegistry"

        private val logger = LoggerFactory.getLogger(ConfigurationClassPostProcessor::class.java)
    }

    // order
    private var order: Int = 0

    // beanClassLoader
    private var classLoader: ClassLoader? = null

    // 环境对象
    private var environment: Environment? = null

    // 配置类的解析器
    private var parser: ConfigurationClassParser? = null

    // 配置类的reader
    private var reader: ConfigurationClassBeanDefinitionReader? = null

    // componentScan的beanNameGenerator，默认使用simpleName作为生成方式
    private var componentScanBeanNameGenerator: BeanNameGenerator = AnnotationBeanNameGenerator.INSTANCE

    // import的Bean的beanNameGenerator，默认使用全限定名作为生成方式
    private var importBeanBeanNameGenerator: BeanNameGenerator = FullyQualifiedAnnotationBeanNameGenerator.INSTANCE

    // 是否设置了局部的BeanGenerator，如果设置了，将会采用默认的BeanNameGenerator
    private var localBeanNameGeneratorSet = false

    // ApplicationStartup
    private var applicationStartup: ApplicationStartup? = null

    /**
     * 设置局部的BeanNameGenerator，设置之后整个扫描过程都会采用给定的BeanNameGenerator作为beanName的生成器
     */
    open fun setBeanNameGenerator(generator: BeanNameGenerator) {
        localBeanNameGeneratorSet = true
        this.importBeanBeanNameGenerator = generator
        this.componentScanBeanNameGenerator = generator
    }

    override fun setApplicationStartup(applicationStartup: ApplicationStartup) {
        this.applicationStartup = applicationStartup
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        processConfigBeanDefinitions(registry)
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        // 如果必要的话，需要尝试去增强配置类(@Configuration)
        enhanceConfigurationClasses(beanFactory)

        // 给容器当中注册处理ImportAware的BeanPostProcessor
        beanFactory.addBeanPostProcessor(ImportAwareBeanPostProcessor(beanFactory))
    }

    open fun processConfigBeanDefinitions(registry: BeanDefinitionRegistry) {
        val configCandidates = ArrayList<BeanDefinitionHolder>()
        val candidateNames = registry.getBeanDefinitionNames()
        candidateNames.forEach { beanName ->
            val beanDef = registry.getBeanDefinition(beanName)
            // 如果已经包含了配置类的属性，说明该配置类已经处理过了，不需要去进行继续处理了
            if (beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE) != null) {
                if (logger.isDebugEnabled) {
                    logger.debug("BeanDefinition[$beanName]已经被当做一个配置类处理过...")
                }
                // 检查配置类是否是配置类，如果是配置类，那么说明它应该作为配置类去进行处理，加入候选列表当中
            } else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef)) {
                configCandidates.add(BeanDefinitionHolder(beanDef, beanName))
            }
        }
        // 如果根本就没有配置过配置类，那么说明不需要去对配置类去进行处理，直接pass掉
        if (configCandidates.isEmpty()) {
            return
        }
        // 按照BeanDefinition当中的order去进行排序(在checkConfigurationClassCandidate当中已经解析过@Order注解了)
        configCandidates.sortWith { o1, o2 ->
            ConfigurationClassUtils.getOrder(o1.beanDefinition)
                .compareTo(ConfigurationClassUtils.getOrder(o2.beanDefinition))
        }

        // 如果没有设置ConfigurationClassPostProcessor局部的BeanNameGenerator
        // 尝试从SingletonBeanRegistry当中去获取到BeanNameGenerator，如果找到了，那么就去替换默认的BeanNameGenerator
        var singletonBeanRegistry: SingletonBeanRegistry? = null
        if (registry is SingletonBeanRegistry && !localBeanNameGeneratorSet) {
            singletonBeanRegistry = registry
            val beanNameGenerator = registry.getSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR)
            if (beanNameGenerator != null) {
                this.importBeanBeanNameGenerator = beanNameGenerator as BeanNameGenerator
                this.componentScanBeanNameGenerator = beanNameGenerator
            }
        }

        val classLoader = this.classLoader ?: throw IllegalStateException("BeanClassLoader不能为空")
        if (this.environment == null) {
            this.environment = StandardEnvironment()
        }

        val candidates = LinkedHashSet(configCandidates)
        val alreadyParsed = HashSet<ConfigurationClass>(configCandidates.size)

        do {
            this.parser = ConfigurationClassParser(registry, environment!!, classLoader, componentScanBeanNameGenerator)
            val parseConfig =
                this.applicationStartup!!.start("spring.context.config-classes.parse")  // start parseConfig

            // 使用配置类解析器去进行解析配置类
            Optional.ofNullable(this.parser).ifPresent { it.parse(candidates) }

            // 获取配置类解析器(ConfigurationClassParser)解析到的所有配置类列表
            val configClasses = LinkedHashSet(parser!!.getConfigurationClasses())
            configClasses.removeAll(alreadyParsed)

            if (reader == null) {
                reader = ConfigurationClassBeanDefinitionReader(
                    registry, importBeanBeanNameGenerator, environment!!, parser!!.getImportRegistry()
                )
            }

            // 加载BeanDefinition
            // 1.如果它是被@Import导入进来的，那么会在这里完成BeanDefinition的注册
            // 2.如果一个配置类有BeanMethod，那么会在这里完成注册
            // 3.如果一个配置类有ImportBeanDefinitionRegistrar，那么会在这里完成导入
            Optional.ofNullable(this.reader).ifPresent { it.loadBeanDefinitions(configClasses) }
            alreadyParsed.addAll(configClasses)
            parseConfig.tag("classCount", "${configClasses.size}").end()  // tag and end

            candidates.clear()
            if (registry.getBeanDefinitionCount() > candidateNames.size) {
                val newCandidateNames = registry.getBeanDefinitionNames()
                val alreadyParsedClasses = alreadyParsed.map { it.configurationClass.name }.toSet()
                newCandidateNames.forEach { beanName ->
                    val beanDefinition = registry.getBeanDefinition(beanName)
                    // 遍历所有的没有被处理过的配置类，去设置配置类属性的检查
                    if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDefinition)
                        && !alreadyParsedClasses.contains(beanDefinition.getBeanClassName())
                    ) {
                        candidates.add(BeanDefinitionHolder(beanDefinition, beanName))
                    }
                }
            }
        } while (candidates.isNotEmpty())

        // 将ImportRegistry注册到beanFactory当中，方便后续处理ImportAware
        if (singletonBeanRegistry != null && !singletonBeanRegistry.containsSingleton(IMPORT_REGISTRY_BEAN_NAME)) {
            singletonBeanRegistry.registerSingleton(IMPORT_REGISTRY_BEAN_NAME, parser!!.getImportRegistry())
        }
    }

    /**
     * 如果必要的话，尝试使用CGLIB去增强配置类
     *
     * @param beanFactory beanFactory
     */
    open fun enhanceConfigurationClasses(beanFactory: ConfigurableListableBeanFactory) {
        val enhanceConfigurationClass = this.applicationStartup!!.start("spring.context.config-classes.enhance")
        val configBeanDefs = LinkedHashMap<String, AbstractBeanDefinition>()
        beanFactory.getBeanDefinitionNames().forEach { beanName ->
            val beanDefinition = beanFactory.getBeanDefinition(beanName)
            val configClassAttr = beanDefinition.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE)
            if (configClassAttr == ConfigurationClassUtils.CONFIGURATION_CLASS_FULL) {
                if (beanDefinition !is AbstractBeanDefinition) {
                    throw IllegalStateException("给定的BeanDefinition不是AbstractBeanDefinition，不支持对该配置类去进行增强")
                } else if (logger.isInfoEnabled && beanFactory.containsSingleton(beanName)) {
                    logger.info("不支持对已经有单例对象的BeanDefinition上去进行增强")
                } else {
                    configBeanDefs[beanName] = beanDefinition
                }
            }
        }
        if (configBeanDefs.isEmpty()) {
            enhanceConfigurationClass.end()
            return
        }

        // 对所有的@Configuration的配置类去进行增强
        val enhancer = ConfigurationClassEnhancer()
        configBeanDefs.forEach { (beanName, beanDefinition) ->
            val beanClass = beanDefinition.getBeanClass()!!
            val configClass = enhancer.enhance(beanClass, classLoader)
            if (configClass != beanClass) {
                if (logger.isTraceEnabled) {
                    logger.trace("替换beanName=[$beanName]的BeanDefinition，从[$beanClass]替换成为了被增强的[$configClass]")
                }
                beanDefinition.setBeanClass(configClass)
            }
        }

        enhanceConfigurationClass.tag("classCount") { configBeanDefs.size.toString() }.end()
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun getOrder(): Int {
        return order
    }

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.classLoader = classLoader
    }

    fun getBeanClassLoader(): ClassLoader {
        return classLoader!!
    }

    open fun getEnvironment(): Environment {
        return this.environment!!
    }

    open fun setOrder(order: Int) {
        this.order = order
    }

    /**
     * 处理ImportAware接口的注入
     */
    private class ImportAwareBeanPostProcessor(private val beanFactory: BeanFactory) :
        InstantiationAwareBeanPostProcessor {

        override fun postProcessProperties(pvs: PropertyValues?, bean: Any, beanName: String): PropertyValues? {
            if (bean is ConfigurationClassEnhancer.EnhancedConfiguration) {
                bean.setBeanFactory(this.beanFactory)
            }
            return pvs
        }

        override fun postProcessBeforeInitialization(beanName: String, bean: Any): Any {
            if (bean is ImportAware) {
                val importRegistry = beanFactory.getBean(IMPORT_REGISTRY_BEAN_NAME, ImportRegistry::class.java)
                val importedMetadata = importRegistry.getImportingClassFor(bean::class.java.name)
                if (importedMetadata != null) {
                    bean.setImportMetadata(importedMetadata)
                }
            }
            return bean
        }
    }
}