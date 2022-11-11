package com.wanna.framework.context.processor.factory.internal

import com.wanna.framework.beans.PropertyValues
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.factory.config.SingletonBeanRegistry
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.definition.AbstractBeanDefinition
import com.wanna.framework.context.ApplicationStartupAware
import com.wanna.framework.context.ResourceLoaderAware
import com.wanna.framework.context.annotation.*
import com.wanna.framework.context.annotation.ConfigurationClassUtils.getOrder
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.context.processor.beans.InstantiationAwareBeanPostProcessor
import com.wanna.framework.context.processor.factory.BeanDefinitionRegistryPostProcessor
import com.wanna.framework.core.PriorityOrdered
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.io.DefaultResourceLoader
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.AnnotationConfigUtils
import com.wanna.framework.util.ClassUtils
import org.slf4j.LoggerFactory

/**
 * SpringBeanFactory的配置类处理器，用来扫描Spring当中的配置类，包括对@Configuration/@Component/@Bean等注解的处理
 *
 * @see ConfigurationClassParser
 * @see ConfigurationClassBeanDefinitionReader
 */
open class ConfigurationClassPostProcessor : BeanDefinitionRegistryPostProcessor, PriorityOrdered, EnvironmentAware,
    BeanClassLoaderAware, ApplicationStartupAware, ResourceLoaderAware {

    companion object {
        /**
         * ImportRegistry的beanName
         */
        @JvmStatic
        private val IMPORT_REGISTRY_BEAN_NAME = ConfigurationClassPostProcessor::class.java.name + ".importRegistry"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(ConfigurationClassPostProcessor::class.java)
    }

    // order
    private var order: Int = 0

    /**
     * beanClassLoader
     */
    @Nullable
    private var classLoader: ClassLoader? = ClassUtils.getDefaultClassLoader()

    /**
     * Spring Environment环境对象
     */
    @Nullable
    private var environment: Environment? = null

    /**
     * 配置类的解析器
     */
    @Nullable
    private var parser: ConfigurationClassParser? = null

    /**
     * 配置类的reader
     */
    @Nullable
    private var reader: ConfigurationClassBeanDefinitionReader? = null

    /**
     * ResourceLoader，提供资源的加载
     */
    private var resourceLoader: ResourceLoader = DefaultResourceLoader()

    // componentScan的beanNameGenerator，默认使用simpleName作为生成方式
    private var componentScanBeanNameGenerator: BeanNameGenerator = AnnotationBeanNameGenerator.INSTANCE

    // import的Bean的beanNameGenerator，默认使用全限定名作为生成方式
    private var importBeanBeanNameGenerator: BeanNameGenerator = FullyQualifiedAnnotationBeanNameGenerator.INSTANCE

    // 是否设置了局部的BeanGenerator，如果设置了，将会采用默认的BeanNameGenerator
    private var localBeanNameGeneratorSet = false

    // ApplicationStartup
    private var applicationStartup: ApplicationStartup? = null

    /**
     * 设置当前的BeanDefinition的局部BeanNameGenerator，设置之后整个扫描过程都会采用给定的BeanNameGenerator作为beanName的生成器
     */
    open fun setBeanNameGenerator(generator: BeanNameGenerator) {
        localBeanNameGeneratorSet = true  // mark to true
        this.importBeanBeanNameGenerator = generator
        this.componentScanBeanNameGenerator = generator
    }

    override fun setApplicationStartup(applicationStartup: ApplicationStartup) {
        this.applicationStartup = applicationStartup
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        processConfigBeanDefinitions(registry)
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        // 如果必要的话，需要尝试去增强配置类(@Configuration)
        enhanceConfigurationClasses(beanFactory)

        // 给BeanFactory当中注册处理ImportAware的BeanPostProcessor
        beanFactory.addBeanPostProcessor(ImportAwareBeanPostProcessor(beanFactory))
    }

    open fun processConfigBeanDefinitions(registry: BeanDefinitionRegistry) {
        val configCandidates = ArrayList<BeanDefinitionHolder>()
        // 先去记录下来先前已经注册到BeanDefinitionRegistry当中的BeanDefinitionNames的列表
        var candidateNames = registry.getBeanDefinitionNames()

        // 遍历registry当中的所有的BeanDefinition，看它是否是一个配置类
        candidateNames.forEach { beanName ->
            val beanDef = registry.getBeanDefinition(beanName)
            // 如果该配置类当中已经包含了配置类的属性，说明该配置类已经处理过了，不需要去进行继续处理了
            if (beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE) != null) {
                if (logger.isDebugEnabled) {
                    logger.debug("BeanDefinition[$beanName]在之前就已经被当做一个配置类处理过...不应该被重复去进行处理")
                }
                // 检查配置类是否是配置类，如果是的话，需要添加到
            } else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef)) {
                configCandidates.add(BeanDefinitionHolder(beanDef, beanName))
            }
        }
        // 如果registry当中根本就没有配置过配置类，那么说明不需要去对配置类去进行处理，直接pass(return)掉就行
        if (configCandidates.isEmpty()) {
            return
        }

        // 对之前加入进来的配置类，按照配置类的order属性去进行排序
        // (Note: 在checkConfigurationClassCandidate当中已经解析过@Order注解放入到BeanDefinition当中了)
        configCandidates.sortWith { o1, o2 -> getOrder(o1.beanDefinition).compareTo(getOrder(o2.beanDefinition)) }

        // 如果没有设置ConfigurationClassPostProcessor局部的BeanNameGenerator，
        // 需要尝试从SingletonBeanRegistry当中去获取到BeanNameGenerator，
        // 如果从SingletonBeanRegistry当中去找到了BeanNameGenerator，那么就去替换默认的BeanNameGenerator
        var singletonBeanRegistry: SingletonBeanRegistry? = null
        if (registry is SingletonBeanRegistry && !localBeanNameGeneratorSet) {
            singletonBeanRegistry = registry
            val beanNameGenerator = registry.getSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR)
            if (beanNameGenerator != null && beanNameGenerator is BeanNameGenerator) {
                this.importBeanBeanNameGenerator = beanNameGenerator
                this.componentScanBeanNameGenerator = beanNameGenerator
            }
        }

        // determine (ClassLoader & Environment & ConfigurationClassParser)  to use
        val classLoader = this.classLoader ?: ClassUtils.getDefaultClassLoader()
        val environment = this.environment ?: StandardEnvironment()
        val parser = this.parser ?: ConfigurationClassParser(
            registry,
            environment,
            classLoader,
            componentScanBeanNameGenerator,
            resourceLoader
        )

        // 候选的，要交给parser去进行配置类解析的BeanDefinition列表(放在循环外，供每次循环所共享)
        val candidates = LinkedHashSet(configCandidates)
        // 已经完成解析的配置类列表，要保证配置类不应该被重复解析(放在循环外，供每次循环所共享)
        val alreadyParsed = HashSet<ConfigurationClass>(configCandidates.size)

        // 只要candidates列表当中，仍旧还要候选的BeanDefinition可以作为配置类的话，那么就一直去进行循环处理
        do {
            val parseConfig =
                this.applicationStartup!!.start("spring.context.config-classes.parse")  // start parseConfig

            // 使用parser去进行解析配置类
            parser.parse(candidates)

            // 获取配置类解析器(ConfigurationClassParser)解析到的所有配置类列表
            val configClasses = LinkedHashSet(parser.getConfigurationClasses())

            // 移除掉之前已经解析过的配置类，这些配置类不应该交给reader去进行二次解析
            configClasses.removeAll(alreadyParsed)

            // determine ConfigurationClassBeanDefinitionReader to use
            val reader = this.reader ?: ConfigurationClassBeanDefinitionReader(
                registry, importBeanBeanNameGenerator, environment, parser.getImportRegistry(), resourceLoader
            )

            // 交给reader去从本次parser解析得到的ConfigClass当中去加载BeanDefinition
            // 1.如果它是被@Import导入进来的，那么会在这里完成BeanDefinition的注册
            // 2.如果一个配置类有BeanMethod，那么会在这里完成注册
            // 3.如果一个配置类有ImportBeanDefinitionRegistrar，那么会在这里完成导入
            reader.loadBeanDefinitions(configClasses)

            // 已经完成解析的列表，应该添加上当前reader已经解析完成的ConfigClass列表
            alreadyParsed.addAll(configClasses)
            parseConfig.tag("classCount", "${configClasses.size}").end()  // tag and end

            // clear掉候选的要去进行解析的配置类列表，因为这些配置类已经被解析过了，不应该被二次解析
            // 我们在后面的代码当中，将会去进行重新统计要去交给parser/reader去进行处理的BeanDefinition列白鸥
            candidates.clear()

            // 如果当前的registry当中的BeanDefinition的数量，已经超过了原来的数量，说明这中间有可能产生了新的BeanDefinition
            // 对于新产生的BeanDefinition，如果它有资格成为一个配置类，但是之前并没有完成过解析，那么说明它应该被加入candidates当中
            // 加入candidates列表当中，将会触发循环被二次执行，交给reader/parser完成candidates当中的配置类的解析工作
            if (registry.getBeanDefinitionCount() > candidateNames.size) {
                val newCandidateNames = registry.getBeanDefinitionNames()
                val alreadyParsedClasses = alreadyParsed.map { it.configurationClass.name }.toSet()
                newCandidateNames.forEach { beanName ->
                    val beanDefinition = registry.getBeanDefinition(beanName)
                    // 遍历所有的没有被处理过的配置类去进行检查，如果它是合格的配置类，但是还未被处理过，将其添加到candidates当中
                    if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDefinition)
                        && !alreadyParsedClasses.contains(beanDefinition.getBeanClassName()!!)
                    ) {
                        candidates.add(BeanDefinitionHolder(beanDefinition, beanName))
                    }
                }
                candidateNames = newCandidateNames  // update last BeanDefinitionNames
            }
            // 如果candidates当中还存在有BeanDefinition的话，那么我们应该去进行二次执行reader/parser的解析工作
            // 为什么还要去进行二次执行？因为有一种可能，就是在BeanDefinitionRegistrar当中，导入了一个配置类的情况
            // 在之前其实是并未处理这个问题的，BeanDefinitionRegistrar导入的配置类，单纯注册但并未支持去进行递归处理
            // 但是我们知道：不管是(1)配置类的内部类/(2)@Import导入的配置类/(3)ImportSelector导入配置类
            // 这几种方式，都是支持递归扫描处理的，而唯独ImportBeanDefinitionRegistrar这种情况，在之前我们都并未
            // 去进行递归处理(也无法处理，因为是用户自己注册的)，单纯导入到registry当中，而二次执行的目的，就是为了去实现递归地去进行处理
        } while (candidates.isNotEmpty())

        // 将ImportRegistry注册到beanFactory当中，方便后续处理ImportAware注解
        // 如果一个注解是被@Import导入进来的，它就支持去注入ImportAware，去获取到导入它的配置类的注解信息
        if (singletonBeanRegistry != null && !singletonBeanRegistry.containsSingleton(IMPORT_REGISTRY_BEAN_NAME)) {
            singletonBeanRegistry.registerSingleton(IMPORT_REGISTRY_BEAN_NAME, parser.getImportRegistry())
        }
    }

    /**
     * 如果必要的话，尝试使用CGLIB去增强目标配置类(针对于所有的"FULL"配置类)
     *
     * @param beanFactory beanFactory
     */
    open fun enhanceConfigurationClasses(beanFactory: ConfigurableListableBeanFactory) {
        val enhanceConfigurationClass = this.applicationStartup!!.start("spring.context.config-classes.enhance")
        val configBeanDefs = LinkedHashMap<String, AbstractBeanDefinition>()
        beanFactory.getBeanDefinitionNames().forEach { beanName ->
            val beanDefinition = beanFactory.getBeanDefinition(beanName)

            // 从BeanDefinition上获取到之前解析的配置类信息(FULL/LITE)，如果是FULL配置类，那么它是一个需要被增强的配置类
            val configClassAttr = beanDefinition.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE)
            if (configClassAttr == ConfigurationClassUtils.CONFIGURATION_CLASS_FULL) {
                if (beanDefinition !is AbstractBeanDefinition) {
                    throw IllegalStateException("给定的BeanDefinition不是AbstractBeanDefinition，不支持对该配置类去进行增强")
                } else if (logger.isInfoEnabled && beanFactory.containsSingleton(beanName)) {
                    logger.info("不支持对已经存在有单例对象的BeanDefinition[beanName=$beanName]上去进行增强")
                } else {
                    configBeanDefs[beanName] = beanDefinition // add full ConfigurationClass
                }
            }
        }

        // 如果没有全配置类，那么直接pass掉，不需要去进行CGLIB子类的生成
        if (configBeanDefs.isEmpty()) {
            enhanceConfigurationClass.end()
            return
        }


        val enhancer = ConfigurationClassEnhancer()

        // 对所有的@Configuration & proxyBeanMethods的配置类去进行增强
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
        enhanceConfigurationClass.tag("classCount") { configBeanDefs.size.toString() }.end()  // tag and end
    }


    override fun getOrder() = this.order

    open fun getBeanClassLoader(): ClassLoader? = this.classLoader

    open fun getEnvironment(): Environment? = this.environment

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.classLoader = classLoader
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    open fun setOrder(order: Int) {
        this.order = order
    }

    /**
     * 处理ImportAware接口的注入的BeanPostProcessor，
     * 如果A配置类导入了B配置类，那么B配置类就可以实现ImportAware，去完成A配置类当中的注解信息的注入，
     * 方便去获取到A当中的注解信息，去完成相关的配置功能
     *
     * @param beanFactory beanFactory
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