package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.BeanDefinitionStoreException
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.beans.factory.support.definition.*
import com.wanna.framework.beans.factory.xml.XmlBeanDefinitionReader
import com.wanna.framework.context.annotation.ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.core.type.StandardMethodMetadata
import com.wanna.framework.util.AnnotationConfigUtils
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.StringUtils
import org.slf4j.LoggerFactory

/**
 * 这是一个配置类的BeanDefinitionReader，负责从ConfigurationClass当中去读取BeanDefinition
 *
 * @see ConfigurationClass
 * @see ConfigurationClassParser
 * @see loadBeanDefinitions
 */
open class ConfigurationClassBeanDefinitionReader(
    private val registry: BeanDefinitionRegistry,
    private val importBeanNameGenerator: BeanNameGenerator,
    private val environment: Environment,
    private val importRegistry: ImportRegistry,
    private val resourceLoader: ResourceLoader
) {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ConfigurationClassBeanDefinitionReader::class.java)


        /**
         * Scope Metadata Resolver
         */
        @JvmStatic
        private val scopeMetadataResolver = AnnotationScopeMetadataResolver()
    }

    /**
     * 这是一个条件计算器，去计算一个Bean是否应该被注册
     */
    private val conditionEvaluator = ConditionEvaluator(registry, environment, resourceLoader)


    /**
     * 从配置类当中加载BeanDefinition，例如@ImportResource/ImportBeanDefinitionRegistrar/@Bean方法
     *
     * @param configurationClasses 通过ConfigurationClassParser解析完成得到的配置类列表
     */
    open fun loadBeanDefinitions(configurationClasses: Collection<ConfigurationClass>) {
        // 创建一个支持轨迹追踪的ConditionEvaluator去进行条件的计算，内部组合ConditionEvaluator完成条件计算
        val trackedConditionEvaluator = TrackedConditionEvaluator()

        // 遍历所有的配置类，去进行BeanDefinition的加载
        configurationClasses.forEach {
            loadBeanDefinitionsForConfigurationClass(it, trackedConditionEvaluator)
        }
    }

    /**
     * 从一个ConfigurationClass当中去加载该配置类当中的BeanDefinition到容器当中，主要包括如下几类：
     * (1)被@Import导入进来的普通配置类，因为没有被处理，需要在这里去进行后期的处理和注册
     * (2)一个ConfigurationClass当中的所有@Bean方法
     * (3)通过@ImportSource配置的，要进行导入的XML配置文件
     * (4)被@Import导入进来的ImportBeanDefinitionRegistrar
     *
     * @param configurationClass 目标配置类
     * @param trackedConditionEvaluator 带轨迹追踪功能的条件计算器
     */
    protected open fun loadBeanDefinitionsForConfigurationClass(
        configurationClass: ConfigurationClass, trackedConditionEvaluator: TrackedConditionEvaluator
    ) {
        // 比较所有导入当前的配置类的配置类是否都已经被移除掉了？如果都已经被移除掉了，那么当前的配置类也应该被移除掉！
        // 如果该配置类应该被skip掉，那么它应该从BeanDefinitionRegistry当中移除掉
        if (trackedConditionEvaluator.shouldSkip(configurationClass)) {
            if (configurationClass.beanName != null && configurationClass.beanName!!.isEmpty()) {
                registry.removeBeanDefinition(configurationClass.beanName!!)
            }
            this.importRegistry.removeImportingClass(configurationClass.metadata.getClassName())
            return
        }

        // 如果它是被@Import注解导入的配置类，那么应该进行处理，并生成beanName注册到BeanDefinitionRegistry当中
        // 在生成beanName的同时，还会为configurationClass去进行beanName的设置
        if (configurationClass.isImportedBy()) {
            registerBeanDefinitionForImportedConfigurationClass(configurationClass)
        }
        // 将所有的BeanMethod去完成匹配，并封装成为BeanDefinition，并注册到registry当中
        configurationClass.beanMethods.forEach(::loadBeanDefinitionsForBeanMethod)

        // 处理@ImportResource，为Annotation版本的IOC容器当中导入XML的Spring配置文件提供支持
        loadBeanDefinitionsFromImportedResources(configurationClass.importedSources)

        // 处理ImportBeanDefinitionRegistrar，交给开发者去往容器当中去实现批量注册BeanDefinition的功能
        loadBeanDefinitionsFromRegistrars(configurationClass.getImportBeanDefinitionRegistrars())
    }

    /**
     * 加载BeanMethod，去将BeanMethod封装成为一个BeanDefinition，并注册BeanDefinition到容器当中
     *
     * @param beanMethod @Bean方法
     */
    open fun loadBeanDefinitionsForBeanMethod(beanMethod: BeanMethod) {
        val metadata = beanMethod.metadata
        // 如果使用条件计算器去进行匹配指导它应该被Skip掉，那么Skip，不进行解析了...
        if (conditionEvaluator.shouldSkip(metadata, REGISTER_BEAN)) {
            return
        }

        val configClass = beanMethod.configClass
        val beanAnnotation = metadata.getAnnotations().get(Bean::class.java)

        // 从@Bean的注解上找到合适的beanName
        val beanName: String = findBeanNameFromBeanAnnotation(beanAnnotation, metadata)

        // 如果这个@Bean方法，在子类当中已经存在了(子类去进行重写)，那么就别添加父类的@Bean方法了，得pass掉...得以子类的为准....
        if (isOverriddenByExistingDefinition(beanMethod, beanName)) {
            if (logger.isDebugEnabled) {
                logger.debug("Spring Config Class ${beanMethod.configClass.metadata.getClassName()} has been registered, cannot register again, beanName=$beanName")
            }
            return
        }

        // 创建一个ConfigurationClassBeanDefinition，让它能适配RootBeanDefinition和AnnotatedBeanDefinition
        // 因为去对注解信息去进行匹配时，需要用到AnnotatedBeanDefinition...
        // 另外一方面也是标识作用，标识它是使用@Bean方法去进行导入的...
        val beanDefinition = ConfigurationClassBeanDefinition(configClass, metadata)

        // set factoryMethodName, factoryBeanName and factoryMethod
        beanDefinition.setFactoryMethodName(metadata.getMethodName())

        // fixed:这里在之前已经生成好了beanName，因此这里可以直接设置beanName即可
        beanDefinition.setFactoryBeanName(configClass.beanName)

        if (metadata is StandardMethodMetadata) {
            beanDefinition.setResolvedFactoryMethod(metadata.getMethod())
            beanDefinition.setBeanClass(metadata.getMethod().returnType)
        } else {

            // 如果不是StandardMethodMetadata的话, 那么也得设置beanClassName
            beanDefinition.setBeanClassName(metadata.getReturnTypeName())
        }


        // 设置autowiredMode为构造器注入
        beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR)

        // 处理@Bean方法上的@Role/@Primary/@DependsOn/@Lazy注解
        AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDefinition, metadata)

        // 根据@Bean注解当中的init方法和destory方法，去设置BeanDefinition的initMethod和destroyMethod
        if (StringUtils.hasText(beanAnnotation.getString("initMethod"))) {
            beanDefinition.setInitMethodName(beanAnnotation.getString("initMethod"))
        }
        if (StringUtils.hasText(beanAnnotation.getString("destroyMethod"))) {
            beanDefinition.setDestroyMethodName(beanAnnotation.getString("destroyMethod"))
        }

        // 设置是否是AutowireCandidate以及AutowireMode
        beanDefinition.setAutowireCandidate(beanAnnotation.getBoolean("autowireCandidate"))
        beanDefinition.setAutowireMode(beanAnnotation.getInt("autowireMode"))

        // 解析scope
        val scope = metadata.getAnnotations().get(Scope::class.java)
        if (scope.present) {
            beanDefinition.setScope(scope.getString("value"))
        }

        // 注册beanDefinition到容器当中
        registry.registerBeanDefinition(beanName, beanDefinition)
    }

    /**
     * 如果之前已经存在过该beanName的BeanDefinition，需要去判断是否应该替换掉之前的BeanDefinition？
     *
     * @param beanMethod 现在的BeanMethod
     * @param beanName 现在的beanName
     * @return 是否应该替换之前的？return true标识pass掉，return false则替换掉...
     */
    protected open fun isOverriddenByExistingDefinition(beanMethod: BeanMethod, beanName: String): Boolean {
        // 如果之前根本就没有存在过该beanName的BeanDefinition，直接return false，去注册BeanMethod
        if (!registry.containsBeanDefinition(beanName)) {
            return false
        }
        val existBeanDef = registry.getBeanDefinition(beanName)

        // 1.如果之前也是一个ConfigurationClassBeanDefinition，说明之前也是在@Bean这里添加的一个Bean...
        if (existBeanDef is ConfigurationClassBeanDefinition) {
            // 判断之前的配置类名和现在的配置类名是否相同？如果相同的话，return true，应该pass掉...
            // 因为配置类处理过程当中，是从子类向父类方向去进行扫描的，因此BeanMethod的遍历顺序也应该是这样的
            // 如果当前BeanMethod和之前的BeanMethod在同一个类当中，说明之前注册的是子类的BeanMethod，现在是父类的BeanMethod
            // 在这种情况下，我们不应该去替换掉之前的BeanMethod，因为我们要以子类的最终实现为准，而不是使用父类的实现
            if (existBeanDef.getMetadata().getClassName() == beanMethod.configClass.metadata.getClassName()) {
                return true
            }

            // 如果之前的@Bean方法所在的配置类和现在的@Bean方法所在的配置类并不相同，那么随便去进行替换。
            return false
        }

        // 2.如果之前的BeanDefinition是被@ComponentScan扫描出来的，那么@Bean方法可以去替换掉它
        // 因为对于@Bean方法的BeanDefinition，拥有更加高的优先级，所以它支持去替换掉原来的@Component的BeanDefinition
        if (existBeanDef is ScannedGenericBeanDefinition) {
            return false
        }

        // 3.对于之前是一个非APPLICATION级别的Bean(是一个基础设施的Bean)，那么可以产生覆盖
        // 因为当前的BeanDefinition是以@Bean方法去进行注册的，Role一定为APPLICATION
        if (existBeanDef.getRole() > BeanDefinition.ROLE_APPLICATION) {
            return false
        }

        // 4.如果BeanFactory本身就不允许发生BeanDefinition的覆盖的话，那么需要丢出异常
        if (this.registry is DefaultListableBeanFactory && !this.registry.isAllowBeanDefinitionOverriding()) {
            throw BeanDefinitionStoreException(
                beanName, "@Bean方法的BeanDefinition尝试去替换掉之前的BeanDefinition $existBeanDef 的操作不合法"
            )
        }
        if (logger.isDebugEnabled) {
            logger.debug(
                "之前已经存在有BeanDefinition[{}]，现在出现了一个新的@Bean方法[{}]，但是不能去替换掉之前的，这里直接skip",
                existBeanDef,
                beanMethod
            )
        }
        return true
    }

    /**
     * 从ImportSource当中去加载BeanDefinition，也就是可以指定xml等类型的配置文件
     */
    open fun loadBeanDefinitionsFromImportedResources(importSources: Map<String, Class<out BeanDefinitionReader>>) {
        importSources.forEach { (resource, readerClass) ->
            // 如果使用默认的Reader
            if (readerClass == BeanDefinitionReader::class.java) {
                XmlBeanDefinitionReader(registry).loadBeanDefinitions(resource)

                // 如果使用了自定义的Reader，那么使用你给定的readerClass去进行加载
            } else {
                BeanUtils.instantiateClass(readerClass, arrayOf(BeanDefinitionRegistry::class.java), arrayOf(registry))
                    .loadBeanDefinitions(resource)
            }
        }
    }

    /**
     * 从BeanDefinitionImportRegistrar当中去加载BeanDefinition到SpringBeanFactory当中
     *
     * @param registrars ImportBeanDefinitionRegistrar列表(value是导入该组件的注解信息)
     */
    open fun loadBeanDefinitionsFromRegistrars(registrars: Map<ImportBeanDefinitionRegistrar, AnnotationMetadata>) {
        registrars.forEach { (registrar, annotationMetadata) ->
            registrar.registerBeanDefinitions(annotationMetadata, registry, importBeanNameGenerator)
        }
    }

    /**
     * 为被导入的配置类去注册BeanDefinition，因为在处理@Import注解时，很多信息都没有存储到ConfigurationClass当中，比如beanName
     * 就没有被处理到，这里需要做一些兜底工作，比如处理一些注解信息和生成beanName
     *
     * Note: 通过Import导入的配置类会在这里被设置好name，为了方便后续@Import导入的配置类的@Bean方法的处理
     */
    private fun registerBeanDefinitionForImportedConfigurationClass(configurationClass: ConfigurationClass) {
        val metadata = configurationClass.metadata
        val beanDefinition = AnnotatedGenericBeanDefinition(metadata)  // 构建一个注解的BeanDefinition

        // 处理@Role/@Primary/@DependsOn/@Lazy注解
        AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDefinition, metadata)

        // 处理@Scope注解
        val scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(beanDefinition)
        beanDefinition.setScope(scopeMetadata.scopeName)

        // 生成beanName
        val beanName = importBeanNameGenerator.generateBeanName(beanDefinition, registry)
        registry.registerBeanDefinition(beanName, beanDefinition)

        // set ConfigurationClass beanName
        configurationClass.beanName = beanName
    }

    /**
     * 这是一个支持轨迹追踪的ConditionEvaluator(条件计算器)，它通过组合ConditionEvaluator完成计算工作；
     * 它支持去判断导入这个配置类的所有的配置类是否都被Skip掉了？如果所有的配置类都被Skip掉了，那么当前的配置类也应该被Skip掉
     *
     * @see com.wanna.framework.context.annotation.ConditionEvaluator
     */
    inner class TrackedConditionEvaluator {
        // 判断是否应该跳过的缓存列表，以ConfigurationClass作为Key去进行缓存，value该配置类是否应该跳过
        private val skipped = HashMap<ConfigurationClass, Boolean>()

        /**
         * 判断这个配置类是否应该被skip掉，如果应该被skip掉，那么这个配置类就应该从已经注册的BeanDefinition当中去移除掉
         */
        fun shouldSkip(configurationClass: ConfigurationClass): Boolean {
            var skip: Boolean? = skipped[configurationClass]
            if (skip == null) {

                // fixed:如果是被导入的配置类才需要匹配所有导入当前配置类的配置类，不然应该跳过这个环节
                if (configurationClass.isImportedBy()) {
                    var allSkipped = true
                    // 判断所有导入这个配置类的配置类，是否都已经被跳过了？使用递归的方式去进行判断
                    // 如果所有都被跳过了，那么这个配置类也应该被skip掉；
                    // 只要导入这个配置类的其中一个配置类还在，那么就有可能不被skip掉
                    for (importedBy in configurationClass.getImportedBy()) {
                        if (!shouldSkip(importedBy)) {
                            allSkipped = false
                            break
                        }
                    }
                    // 如果确实全部都被skip掉了，那么skip=true，表示当前的配置类也应该被Skip掉
                    if (allSkipped) {
                        skip = true
                    }
                }
                // 如果不是全部都被skip掉了，那么需要计算一下是否应该被匹配？
                if (skip == null) {
                    skip = conditionEvaluator.shouldSkip(configurationClass.metadata, REGISTER_BEAN)
                }
                skipped[configurationClass] = skip  // put Cache
            }
            return skip
        }
    }

    /**
     * 从@Bean的注解上找到合适的beanName；
     * * 1.尝试使用name属性
     * * 2.尝试使用value属性
     * * 3.尝试使用方法名
     *
     * @return 解析到的beanName
     */
    private fun findBeanNameFromBeanAnnotation(
        beanAnnotation: MergedAnnotation<Bean>, metadata: MethodMetadata
    ): String {
        var beanName: String? = null
        if (StringUtils.hasText(beanAnnotation.getString("name"))) {
            beanName = beanAnnotation.getString("name")
        }
        if (!StringUtils.hasText(beanName) && StringUtils.hasText(beanAnnotation.getString("value"))) {
            beanName = beanAnnotation.getString("value")
        }
        if (!StringUtils.hasText(beanName)) {
            beanName = metadata.getMethodName()
        }
        return beanName!!
    }

    /**
     * 因为RootBeanDefinition本身不是AnnotatedBeanDefinition，但是这里需要用到AnnotatedBeanDefinition；
     * 才能去处理@Lazy/@Primary/@Role/@DependsOn注解，因此这里需要做一层的适配
     *
     * @param configurationClass 配置类
     * @param methodMetadata 方法的MethodData
     */
    class ConfigurationClassBeanDefinition(
        private val configurationClass: ConfigurationClass, private val methodMetadata: MethodMetadata?
    ) : RootBeanDefinition(), AnnotatedBeanDefinition {
        init {
            // 设置Resource
            setResource(configurationClass.resource)
        }

        override fun getMetadata(): AnnotationMetadata = configurationClass.metadata

        /**
         * 重写父类的getMetadata方法，去提供FactoryMethod的Metadata的获取
         */
        override fun getFactoryMethodMetadata(): MethodMetadata? = this.methodMetadata
    }
}