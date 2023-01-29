package com.wanna.framework.context.annotation

import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.beans.BeanUtils
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.BeanDefinitionReader
import com.wanna.framework.beans.factory.support.BeanNameGenerator
import com.wanna.framework.beans.factory.support.definition.AbstractBeanDefinition
import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotation.ConfigurationCondition.ConfigurationPhase
import com.wanna.framework.context.annotation.DeferredImportSelector.Group
import com.wanna.framework.context.annotation.DeferredImportSelector.Group.Entry
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.annotation.*
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.environment.CompositePropertySource
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.core.io.support.DefaultPropertySourceFactory
import com.wanna.framework.core.io.support.PropertySourceFactory
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.core.type.StandardAnnotationMetadata
import com.wanna.framework.core.type.classreading.MetadataReader
import com.wanna.framework.core.type.classreading.MetadataReaderFactory
import com.wanna.framework.core.type.filter.AssignableTypeFilter
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.*
import java.util.function.Predicate

/**
 * 这是一个配置类的解析器, 用来扫描配置类相关的注解, 将其注册到容器当中
 */
open class ConfigurationClassParser(
    private val registry: BeanDefinitionRegistry,
    private val environment: Environment,
    private val componentScanBeanNameGenerator: BeanNameGenerator,
    private val resourceLoader: ResourceLoader,
    private val metadataReaderFactory: MetadataReaderFactory
) {
    companion object {

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ConfigurationClass::class.java)

        /**
         * 默认情况下的[PropertySourceFactory], 用于去创建PropertySource, 并注册到Spring的[Environment]当中
         */
        @JvmStatic
        private val DEFAULT_PROPERTY_SOURCE_FACTORY = DefaultPropertySourceFactory()

        /**
         * DeferredImportSelectorHolder的比较器, 因为对[DeferredImportSelector]包装了一层,
         * 因此对于Comparator也需要去进行适配一层
         */
        @JvmStatic
        private val DEFERRED_IMPORT_SELECTOR_COMPARATOR = Comparator<DeferredImportSelectorHolder> { o1, o2 ->
            AnnotationAwareOrderComparator.INSTANCE.compare(o1.deferredImportSelector, o2.deferredImportSelector)
        }

        /**
         * 默认的用来去对配置类去进行排除的断言Filter, 被这个Filter匹配上的配置类, 将不会被注册...
         */
        @JvmStatic
        private val DEFAULT_EXCLUSION_FILTER =
            Predicate<String> { it.startsWith("java.") || it.startsWith("com.wanna.framework.context.stereotype.") }
    }

    /**
     * ComponentScan注解的解析器
     */
    private val parser: ComponentScanAnnotationParser = ComponentScanAnnotationParser(
        this.registry, this.environment, this.resourceLoader, componentScanBeanNameGenerator
    )

    /**
     * 条件计算器, 通过@Conditional注解去计算该Bean是否应该被导入到容器当中?
     */
    private val conditionEvaluator = ConditionEvaluator(this.registry, this.environment, resourceLoader)

    /**
     * 维护了扫描出来的ConfigurationClass的集合
     */
    private val configClasses = LinkedHashMap<ConfigurationClass, ConfigurationClass>()

    /**
     * 已经知道的配置类(Key-ClassName, Value-ConfigurationClass)
     */
    private val knownClasses = LinkedHashMap<String, ConfigurationClass>()

    /**
     * 这是一个要进行延时处理的ImportSelector列表, 需要完成所有的配置类的解析之后, 才去进行处理;
     * **SpringBoot完成自动配置, 就是通过DeferredImportSelector去完成的**
     */
    private val deferredImportSelectorHandler = DeferredImportSelectorHandler()

    /**
     * Import栈, 一方面注册importedClass与导入它的配置类的元信息, 一方面记录Import过程当中的栈轨迹(判断是否发生了循环导入)
     */
    private val importStack = ImportStack()

    /**
     * Object类的SourceClass
     */
    private val objectSourceClass = SourceClass(Any::class.java)

    /**
     * 获取导入被@Import配置类的信息的注册中心(导入栈), 用来处理ImportAware接口的注入Metadata信息
     *
     * @return ImportRegistry
     */
    open fun getImportRegistry(): ImportRegistry = this.importStack

    /**
     * 获取当前[ConfigurationClassParser]去解析完成的配置类列表
     *
     * @return 解析完成的配置类列表
     */
    open fun getConfigurationClasses(): Set<ConfigurationClass> = this.configClasses.keys

    /**
     * 解析容器中已经有的BeanDefinition当中的相关导入组件的配置类;
     * 一个BeanDefinitionHolder当中维护了beanDefinition和beanName信息
     *
     * @param candidates 候选的BeanDefinitionHolder
     * @see BeanDefinitionHolder
     */
    open fun parse(candidates: Collection<BeanDefinitionHolder>) {
        candidates.forEach { parse(it.beanDefinition, it.beanName) }

        // 在处理完所有的应该扫描的相关配置类之后, 应该去进行处理延时加载的ImportSelector
        // 比如SpringBoot的自动装配, 就会在这里去完成, 它的执行时期, 比普通的Bean的处理更晚
        deferredImportSelectorHandler.process()
    }

    /**
     * 针对指定的单个BeanDefinition, 根据BeanDefinition的具体类型, 使用不同的方法去执行解析
     *
     * @param beanDefinition 待处理的beanDefinition
     * @param beanName beanName
     */
    open fun parse(beanDefinition: BeanDefinition, beanName: String) {
        when (beanDefinition) {
            is AnnotatedBeanDefinition -> parse(beanDefinition.getMetadata(), beanName)
            is AbstractBeanDefinition -> parse(beanDefinition.getBeanClass()!!, beanName)
            else -> parse(beanDefinition.getBeanClassName(), beanName)
        }
    }

    /**
     * 针对给定的ClassName和beanName去构建出来ConfigurationClass, 并进行处理
     *
     * @param className className
     * @param beanName beanName
     */
    open fun parse(className: String?, beanName: String) {
        className ?: throw IllegalStateException("className cannot be null")
        processConfigurationClass(
            ConfigurationClass(this.metadataReaderFactory.getMetadataReader(className), beanName),
            DEFAULT_EXCLUSION_FILTER
        )
    }

    /**
     * 根据给定的AnnotationMetadata去构建ConfigurationClass, 并进行处理
     *
     * @param metadata metadata
     * @param beanName beanName
     */
    open fun parse(metadata: AnnotationMetadata, beanName: String) {
        processConfigurationClass(ConfigurationClass(metadata, beanName), DEFAULT_EXCLUSION_FILTER)
    }

    /**
     * 给定指定的beanClass和beanName, 去构建配置类, 并去进行处理
     *
     * @param beanClass beanClass
     * @param beanName beanName
     */
    open fun parse(beanClass: Class<*>, beanName: String) {
        processConfigurationClass(ConfigurationClass(beanClass, beanName), DEFAULT_EXCLUSION_FILTER)
    }

    /**
     * 将给定的[ConfigurationClass]去转换成为[SourceClass]
     *
     * @param configClass ConfigurationClass
     * @param filter 配置类的过滤的断言Filter, 匹配上的配置类将不会被注册
     * @return 将该配置类去转换之后得到的[SourceClass]
     */
    private fun asSourceClass(configClass: ConfigurationClass, filter: Predicate<String>): SourceClass {
        val metadata = configClass.metadata

        // 如果是StandardAnnotationMetadata, 那么使用Class去创建SourceClass
        if (metadata is StandardAnnotationMetadata) {
            return asSourceClass(metadata.introspectedClass, filter)
        }

        // 如果不是StandardAnnotationMetadaa, 那么使用className, 基于MetadataReader去创建SourceClass
        return asSourceClass(metadata.getClassName(), filter)
    }

    /**
     * 将一个配置类去作为sourceClass
     *
     * @param clazz clazz
     * @param filter 对配置类去进行匹配的断言Filter
     * @return SourceClass
     */
    private fun asSourceClass(clazz: Class<*>, filter: Predicate<String>): SourceClass {
        // 如果该类被Filter过滤掉, 那么直接pass...
        if (filter.test(clazz.name)) {
            return objectSourceClass
        }
        try {
            // 对该类上的注解, 去进行逐一检验, 避免出现了注解的属性当中配置了一些当前VM当中不存在的类
            // 比如"@ConditionalOnClass([Gson::class])", 这个Gson类就很可能不在我们的VM当中
            for (annotation in clazz.declaredAnnotations) {
                AnnotationUtils.validateAnnotation(annotation)
            }

            return SourceClass(clazz)
        } catch (ex: Throwable) {
            // trace记录一下...
            if (logger.isTraceEnabled) {
                logger.trace("Sanity test meet invalid annotation attributes, enforce to use asm via class name", ex)
            }
            // 如果检验注解失败的话, 那么只能尝试强制使用ASM去读取该类的信息...
            return asSourceClass(clazz.name, filter)
        }
    }

    /**
     * 将className去作为SourceClass
     *
     * @param className className
     * @param filter 执行配置类的过滤的过滤器断言
     * @return SourceClass
     */
    private fun asSourceClass(className: String, filter: Predicate<String>): SourceClass {
        // 如果该类被Filter过滤掉, 那么直接pass...
        if (filter.test(className)) {
            return objectSourceClass
        }

        // 对于JavaType, 永远别尝试使用ASM去进行类的加载...
        if (className.startsWith("java")) {
            try {
                return SourceClass(ClassUtils.forName<Any>(className, this.resourceLoader.getClassLoader()))
            } catch (ex: ClassNotFoundException) {
                throw IllegalStateException("Failed to load class '$className'", ex)
            }
        }

        // 使用MetadataReader的方式, 去构建SourceClass
        return SourceClass(metadataReaderFactory.getMetadataReader(className))
    }

    /**
     * 执行解析配置类, 需要递归处理该配置类的所有的父类
     *
     * @param configClass 目标配置类
     * @param filter 对配置类去进行匹配的断言Filter
     */
    private fun processConfigurationClass(configClass: ConfigurationClass, filter: Predicate<String>) {
        // 如果条件计算器计算得知需要去进行pass掉, 那么在这里直接pass掉, 它要导入的所有组件都pass掉
        if (conditionEvaluator.shouldSkip(configClass.metadata, ConfigurationPhase.PARSE_CONFIGURATION)) {
            return
        }

        // 如果已经处理过了, 那么return...
        if (configClasses.containsKey(configClass)) {
            return
        }

        // 将当前正在处理的配置类注册到已有的配置类当中, 避免出现StackOverflow的情况...
        configClasses[configClass] = configClass

        // 把ConfigurationClass转为sourceClass去进行匹配...sourceClass有可能为它的父类这种情况...
        // 这里需要递归处理它的所有父类, 都当做配置类去进行处理, 但是它的父类当中的所有BeanMethod、ImportResource、ImportBeanDefinitionRegistrar都保存到configClass当中
        var sourceClass: SourceClass? = asSourceClass(configClass, filter)  // 最开始, 把configClass当做sourceClass即可
        do {
            sourceClass = doProcessConfigurationClass(configClass, sourceClass!!, filter)
        } while (sourceClass != null)
    }

    /**
     * 交给这个方法, 去进行真正地去处理一个配置类
     *
     * @param configClass 目标配置类(用于存放BeanMethod/ImportResource/ImportBeanDefinitionRegistrar/PropertySource等), 不会进行匹配的操作
     * @param sourceClass 源类(目标配置类的直接/间接父类), 对它去进行注解的扫描并将扫描的结果存放到[configClass]当中
     * @param filter 用来去对配置类进行排除的Filter, 符合filter的要求(filter.test==true)的将会被排除掉
     * @return 如果[sourceClass]还存在有父类的话, 返回父类的[SourceClass]; 如果没有父类的话, return null
     */
    @Nullable
    private fun doProcessConfigurationClass(
        configClass: ConfigurationClass, sourceClass: SourceClass, filter: Predicate<String>
    ): SourceClass? {
        // Note: 如果该配置类有@Component注解, 那么它有资格去处理内部类
        if (configClass.metadata.isAnnotated(Component::class.java.name)) {
            processMemberClasses(configClass, sourceClass, filter)
        }

        // 处理@PropertySource注解
        processPropertySources(configClass, sourceClass)

        // 处理@ComponentScan注解
        processComponentScans(sourceClass)

        // 处理@ImportSource注解
        processImportSources(configClass, sourceClass)

        // 处理@Import注解
        processImports(configClass, sourceClass, getImports(sourceClass), filter)

        // 处理@Bean注解
        processBeanMethods(configClass, sourceClass)

        // 递归处理sourceClass的所有的接口上的@Bean注解的方法
        processInterfaces(configClass, sourceClass)

        // 如果还有superClass, 那么return superClass(并不一定是真的Class, 可能还是通过MetadataReader提供访问的SourceClass)
        if (sourceClass.metadata.hasSuperClass()) {
            val superClassName = sourceClass.metadata.getSuperClassName()
            if (superClassName != null
                && !superClassName.startsWith("java.")
                && !knownClasses.containsKey(superClassName)
            ) {
                knownClasses[superClassName] = configClass
                return sourceClass.getSuperClass()
            }
        }
        return null
    }

    /**
     * 如果一个类上标注了`@Component`注解的话, 那么我们需要去处理每个配置类的内部的成员类
     *
     * @param configClass 标注了`@Component`注解的目标配置类
     * @param sourceClass 源类(有可能为configClass/configClass的父类)
     * @param filter 执行对于配置类的过滤的断言Filter
     */
    private fun processMemberClasses(
        configClass: ConfigurationClass, sourceClass: SourceClass, filter: Predicate<String>
    ) {
        // 获取该类的所有的MemberClass
        val memberClasses = sourceClass.getMemberClasses()
        if (memberClasses.isNotEmpty()) {
            val candidates = ArrayList<SourceClass>()
            for (memberClass in memberClasses) {
                // 如果它标注了@Import/@ImportResource/@PropertySource/@ComponentScan注解,
                // 或者它的内部存在有@Bean方法, 那么它就是一个合格的候选配置类
                if (ConfigurationClassUtils.isConfigurationCandidate(memberClass.metadata)
                    && memberClass.metadata.getClassName() != configClass.metadata.getClassName()
                ) {
                    candidates += memberClass
                }
            }

            // 遍历所有的内部类, 去进行递归处理...设置ImportedBy, beanName将会在后期Reader当中去进行生成(内部类其实和@Import导入的完全等效啊...)
            candidates.forEach {
                if (importStack.contains(configClass)) {
                    logger.info("[${configClass}]出现了循环导入的情况...")
                } else {
                    importStack.push(configClass)
                    try {
                        // 根据内部类, 去构建一个ConfigurationClass, 设置importedBy为外部类...(它被外部类所导入)
                        processConfigurationClass(it.asConfigClass(configClass), filter)  // 把成员类当做配置类去递归处理...
                    } finally {
                        importStack.pop()
                    }
                }
            }
        }
    }

    /**
     * 处理某个配置类上的@PropertySource注解, 将该PropertySource导入的locations当中的资源, 添加到Spring Environment当中
     *
     * @param configClass 要去进行匹配的目标配置类
     * @param sourceClass 源类
     */
    private fun processPropertySources(configClass: ConfigurationClass, sourceClass: SourceClass) {
        // 获取到SourceClass当中的PropertySource注解的属性信息...
        val propertySources = AnnotationConfigUtils.attributesForRepeatable(
            sourceClass.metadata,
            PropertySource::class.java,
            PropertySource::class.java
        )
        for (propertySource in propertySources) {
            val name =
                if (!StringUtils.hasText(propertySource.getString("name"))) null else propertySource.getString("name")
            // 是否需要忽略未找到的资源? 
            val ignoreResourceNotFound = propertySource.getBoolean("ignoreResourceNotFound")
            val locations = propertySource.getStringArray("locations")
            if (locations.isEmpty()) {
                throw IllegalStateException("@PropertySource(value)必须配置至少一个资源路径")
            }

            // 创建PropertySourceFactory, 交给它去完成配置文件(Properties)的加载工作...
            // 如果有自定义PropertySourceFactory的话, 那么需要使用用户自定义的PropertySourceFactory
            val factoryClass = propertySource.getClass("factory")
            val propertySourceFactory =
                if (factoryClass == PropertySourceFactory::class.java) DEFAULT_PROPERTY_SOURCE_FACTORY
                else BeanUtils.instantiateClass(factoryClass) as PropertySourceFactory

            // 遍历给定的所有资源的位置(location), 使用PropertySourceFactory去进行加载
            locations.forEach {
                try {
                    // location支持使用占位符, 这里需要去进行解析占位符
                    val location = this.environment.resolveRequiredPlaceholders(it)
                    val resource = resourceLoader.getResource(location)
                    addPropertySource(propertySourceFactory.createPropertySource(name, resource))
                } catch (ex: Exception) {
                    if (ex is IllegalArgumentException || ex is SocketException || ex is FileNotFoundException || ex is UnknownHostException) {
                        if (ignoreResourceNotFound) {
                            logger.info("给定的资源路径[$it]未找到, 将会被忽略掉...")
                            return
                        }
                    }
                    throw ex
                }
            }
        }
    }

    /**
     * 添加一个PropertySource到Spring Environment当中
     *
     * @param propertySource 要去进行添加的PropertySource
     */
    private fun addPropertySource(propertySource: com.wanna.framework.core.environment.PropertySource<*>) {
        val name = propertySource.name
        val propertySources = (this.environment as ConfigurableEnvironment).getPropertySources()

        if (propertySources.contains(name)) {
            val oldPropertySource = propertySources.get(name)!!

            // 如果之前就是CompositePropertySource, 那么直接添加到之前的后面就行
            if (oldPropertySource is CompositePropertySource) {
                oldPropertySource.addPropertySource(propertySource)

                // 如果之前还不是组合的PropertySource, 那么需要组合旧的和新的
            } else {
                val composite = CompositePropertySource(name)
                composite.addPropertySource(oldPropertySource)
                composite.addPropertySource(propertySource)
                propertySources.replace(name, composite)  // replace
            }

            // 如果之前都还没存在过该name的PropertySource, 直接addLast到Environment当中
        } else {
            propertySources.addLast(propertySource)
        }
    }

    /**
     * 对[SourceClass]的所有的接口, 去进行处理, 将接口当中的[Bean]的方法去收集到[configClass]当中
     *
     * @param configClass ConfigurationClass
     * @param sourceClass 当前正在处理的SourceClass
     */
    private fun processInterfaces(configClass: ConfigurationClass, sourceClass: SourceClass) {
        for (itf in sourceClass.getInterfaces()) {

            // 从该接口当中去提取到所有的@Bean方法...
            val beanMethods = retrieveBeanMethodMetadata(itf)
            for (beanMethod in beanMethods) {
                if (!beanMethod.isAbstract()) {
                    // 添加Java8+当中新增的实现的默认的@Bean方法
                    configClass.addBeanMethod(BeanMethod(beanMethod, configClass))
                }
            }

            // 将接口作为SourceClass去进行递归处理
            processInterfaces(configClass, itf)
        }
    }

    /**
     * 处理`@Bean`注解的标注的方法, 将所有的`@Bean`方法加入到候选列表当中
     *
     * @param configClass ConfigurationClass, 用于收集@Bean方法
     * @param sourceClass 正在处理的SourceClass
     */
    private fun processBeanMethods(configClass: ConfigurationClass, sourceClass: SourceClass) {
        val beanMethods = retrieveBeanMethodMetadata(sourceClass)
        for (metadata in beanMethods) {
            configClass.addBeanMethod(BeanMethod(metadata, configClass))
        }
    }

    /**
     * 从给定的[SourceClass]当中去提取出来所有的标注了`@Bean`注解的方法
     *
     * @param sourceClass SourceClass
     * @return 从该类当中提取到的[Bean]方法列表
     */
    private fun retrieveBeanMethodMetadata(sourceClass: SourceClass): Set<MethodMetadata> {
        val metadata = sourceClass.metadata
        var beanMethods = metadata.getAnnotatedMethods(Bean::class.java.name)

        // 如果Metadata是StandardAnnotationMetadata的话, 那么说明是使用的反射的方式去获取到的@Bean方法
        // 但是不幸的是, JVM标准的反射返回的方法顺序不固定, 甚至有可能在相同的JVM下运行相同的应用, 都能拿到不同的结果
        // 我们尝试使用ASM的方式去获取到用户定义的顺序...(实在无法获取到就不管了, 就用标准反射的结果就行了...)
        if (beanMethods.size > 1 && metadata is StandardAnnotationMetadata) {
            try {
                val asm = metadataReaderFactory.getMetadataReader(metadata.getClassName()).annotationMetadata
                val asmMethods = asm.getAnnotatedMethods(Bean::class.java.name)
                if (asmMethods.size >= beanMethods.size) {
                    // ASM的方法和JDK标准反射的方法去进行匹配, 将对应的ASM方法存起来作为最终结果
                    val selectedMethods = LinkedHashSet<MethodMetadata>()
                    for (asmMethod in asmMethods) {
                        for (beanMethod in beanMethods) {
                            if (beanMethod.getMethodName() == asmMethod.getMethodName()) {
                                selectedMethods += asmMethod
                                break
                            }
                        }
                    }

                    // 如果所有的方法都通过ASM的方式去找到了, 那么使用ASM的结果去作为最终的结果...
                    // 否则的话, 仍旧使用原来的标准反射的方式去寻找到的结果去进行返回
                    if (selectedMethods.size == beanMethods.size) {
                        beanMethods = selectedMethods
                    }
                }
            } catch (ex: IOException) {
                logger.debug("Failed to read class file via ASM for determining @Bean method order")
                // ignore, 我们还有标准反射的@Bean方法可以用呢...
            }
        }

        return beanMethods
    }

    /**
     * 处理@ImportSource注解, 这个注解的作用是为别的方式导入Bean提供支持; 比如在注解版的IOC容器当中, 去提供对XML配置文件的处理
     *
     * @see ImportResource
     * @see BeanDefinitionReader 如何导入组件? 通过自定义BeanDefinitionReader的方式去进行导入组件
     */
    @Suppress("UNCHECKED_CAST")
    private fun processImportSources(configClass: ConfigurationClass, sourceClass: SourceClass) {
        val importResource = AnnotationConfigUtils.attributesFor(sourceClass.metadata, ImportResource::class.java)
        if (importResource != null) {
            val locations = importResource.getStringArray("locations")
            val reader = importResource.getClass("reader") as Class<BeanDefinitionReader>
            for (location in locations) {
                val resolvedLocation = environment.resolveRequiredPlaceholders(location)
                configClass.addImportSource(resolvedLocation, reader)
            }
        }
    }

    /**
     * 处理Import注解
     * (1)如果@Import导入的是一个ImportSelector, 那么把它的selectImports方法返回的组件当做候选的Import组件去进行继续处理
     * ----如果是一个DeferredImportSelector, 那么把它保存到DeferredImportSelectorHandler当中, 等待配置类处理完之后再去进行处理
     * (2)如果@Import导入的是一个ImportBeanDefinitionRegistrar, 那么需要把它保存到配置类当中, 等待后续回调
     * (3)如果@Import导入的是一个普通的组件, 那么把它当做一个普通的配置类去进行递归处理
     *
     * @param configClass 标注@Import的配置类
     * @param importCandidates @Import导入的配置类列表
     * @param exclusionFilter 要进行排除的Filter
     */
    private fun processImports(
        configClass: ConfigurationClass,
        currentSourceClass: SourceClass,
        importCandidates: Collection<SourceClass>,
        exclusionFilter: Predicate<String>
    ) {
        // 如果没有找到候选的要进行Import的组件, 那么直接return
        if (importCandidates.isEmpty()) {
            return
        }
        importStack.push(configClass)  // push stack
        try {
            importCandidates.forEach { candidate ->
                // 如果它是一个ImportSelector(不会注册到容器当中)
                if (candidate.isAssignable(ImportSelector::class.java)) {
                    val candidateClass = candidate.loadClass()
                    val selector = ParserStrategyUtils.instanceClass<ImportSelector>(
                        candidateClass, this.environment, this.registry, this.resourceLoader
                    )

                    // 如果selector使用了排除的Filter, 那么需要将它与exclusionFilter进行或运算
                    // 表示只要其中一个符合, 那么就不匹配...
                    val selectorExclusionFilter = selector.getExclusionFilter()
                    var exclusionFilterToUse = exclusionFilter
                    if (selectorExclusionFilter != null) {
                        exclusionFilterToUse = exclusionFilterToUse.or(selectorExclusionFilter)
                    }

                    // 如果它是一个延时处理的ImportSelector, 那么需要缓存起来, 后续一起去进行处理
                    if (selector is DeferredImportSelector) {
                        deferredImportSelectorHandler.add(configClass, selector)
                    } else {
                        val imports = selector.selectImports(currentSourceClass.metadata)
                        val sourceClasses = asSourceClasses(imports, exclusionFilterToUse)
                        // 递归处理Import导入的Selector
                        processImports(configClass, currentSourceClass, sourceClasses, exclusionFilterToUse)
                    }
                    // 如果它是一个ImportBeanDefinitionRegistrar(不会注册到容器当中)
                } else if (candidate.isAssignable(ImportBeanDefinitionRegistrar::class.java)) {
                    val candidateClass = candidate.loadClass()
                    // 实例化, 并保存ImportBeanDefinitionRegistrar到configClass当中
                    val registrar = ParserStrategyUtils.instanceClass<ImportBeanDefinitionRegistrar>(
                        candidateClass, this.environment, this.registry, this.resourceLoader
                    )
                    // value为配置类中的相关的的注解信息, 在后续去回调ImportBeanDefinitionRegistrar时会以参数的形式传递给调用方
                    configClass.addRegistrar(registrar, currentSourceClass.metadata)
                    // 如果只是导入了一个普通组件, 需要把它当做一个配置类去进行递归处理
                } else {
                    // 注册Import导入的配置类的信息(第一个参数是被导入的配置类名(key), 第二个参数是导入它的配置类的注解信息(value))
                    importStack.registerImport(candidate.metadata.getClassName(), currentSourceClass.metadata)
                    // 构建被导入的配置类信息, beanName等ConfigurationClassBeanDefinitionReader.registerBeanDefinitionForImportedConfigurationClass生成
                    processConfigurationClass(candidate.asConfigClass(configClass), exclusionFilter)  // 把当前类当做配置类去进行递归
                }
            }
        } finally {
            importStack.pop()  // pop stack
        }
    }

    /**
     * 获取ImportSelector的返回值导入的的候选配置类
     *
     * @param imports ImportSelector的selectImports方法导入的Class列表
     * @param exclusionFilter 用于排除使用到的filter
     * @return ImportSelector导入的配置类列表
     */
    private fun asSourceClasses(imports: Array<String>, exclusionFilter: Predicate<String>): Collection<SourceClass> {
        return imports.map { asSourceClass(it, exclusionFilter) }
    }

    /**
     * 获取通过@Import注解导入的那些SourceClass(Note: 对于这里的@Import注解需要去进行递归处理, 考虑那些Meta注解的情况)
     *
     * @param sourceClass sourceClass
     * @return 通过@Import导入进来的SourceClass列表
     */
    private fun getImports(sourceClass: SourceClass): Collection<SourceClass> {
        val imported = LinkedHashSet<SourceClass>()
        val visited = LinkedHashSet<SourceClass>()

        // 进行递归的@Import导入的配置类的收集
        collectImports(sourceClass, imported, visited)
        return imported
    }

    /**
     * 收集起来所有的@Import注解的相关信息
     *
     * @param sourceClass sourceClass
     */
    private fun collectImports(
        sourceClass: SourceClass, imported: MutableCollection<SourceClass>, visited: MutableCollection<SourceClass>
    ) {
        if (visited.add(sourceClass)) {

            // 递归遍历它的所有的注解...我们本来应该获取所有的MergedAnnotation的,
            // 但是很可惜的是, 我们这里拿不到真实的类, 因此无法使用MergedAnnotation, 只能使用手动递归的方式了
            for (annotation in sourceClass.getAnnotations()) {
                val annName = annotation.metadata.getClassName()
                if (annName != Import::class.java.name) {
                    collectImports(annotation, imported, visited)
                }
            }

            // 如果当前注解是@Import注解的话, 那么我们需要收集起来它的value当中配置的所有的配置类
            val mergedAnnotation = sourceClass.metadata.getAnnotations().get(Import::class.java)
            if (!mergedAnnotation.present) {
                return
            }

            for (importedClass in mergedAnnotation.getClassArray(MergedAnnotation.VALUE)) {
                imported += asSourceClass(importedClass, DEFAULT_EXCLUSION_FILTER)
            }
        }
    }

    /**
     * 处理sourceClass配置类上@ComponentScan注解
     *
     * @param sourceClass 要寻找@ComponentScan注解的源类
     */
    private fun processComponentScans(sourceClass: SourceClass) {
        // 找到SourceClass上的ComponentScan注解列表
        val componentScans = AnnotationConfigUtils.attributesForRepeatable(
            sourceClass.metadata,
            ComponentScan::class.java,
            ComponentScan::class.java
        )
        if (componentScans.isEmpty()) {
            return
        }
        // 如果有@ComponentScan注解, 并且条件计算器计算不应该跳过, 那么才需要遍历所有的ComponentScan注解去进行处理
        if (!this.conditionEvaluator.shouldSkip(sourceClass.metadata, ConfigurationPhase.REGISTER_BEAN)) {
            componentScans.forEach { attributes ->
                // 处理@ComponentScan注解, 将符合条件的BeanDefinition, 导入到容器当中
                // 并且应该将@ComponentScan扫描进来的BeanDefinition, 通通当做一个配置类去进行解析, 递归
                parse(parser.parse(attributes, sourceClass.metadata.getClassName()))
            }
        }
    }

    /**
     * 描述了要去进行解析的一个配置类的相关信息, 对于一个配置类来说, 可能会存在有多个父类,
     * 对于一个配置类以及它的所有的父类, 最终都应该当做一个[SourceClass]去进行递归处理.
     *
     * 正常来讲, [source]应该是一个Class的, 但是很可惜的是, 现在时机太早了, 我们根本无法做到, 我们不应该这么早的去进行类加载,
     * 因此在这里我们换种方式, 尽可能不去进行类加载, 我们需要尽可能使用ASM的方式, 利用[MetadataReader]去进行读取类的相关信息,
     * 对于[MetadataReader]将会尽可能采用ASM读取Class文件的方式去进行实现, 可以实现不进行类的加载就读取到的类的相关信息
     *
     * @param source source(Class/MetadataReader)
     * @see MetadataReader
     */
    private inner class SourceClass(val source: Any) {
        /**
         * 该类的AnnotationMetadata信息
         */
        val metadata =
            if (source is Class<*>) AnnotationMetadata.introspect(source) else (source as MetadataReader).annotationMetadata

        /**
         * 将[SourceClass]转换为[ConfigurationClass]
         *
         * @param importedBy 它是被哪个类导入进来的?
         * @return 基于当前[SourceClass]去构建好的ConfigurationClass
         */
        fun asConfigClass(importedBy: ConfigurationClass): ConfigurationClass {
            return if (source is Class<*>) {
                ConfigurationClass(source, importedBy)
            } else {
                ConfigurationClass(source as MetadataReader, importedBy)
            }
        }

        /**
         * 判断它是否和某个类型匹配?
         *
         * @param parentClass 父类
         * @return 当前clazz是否是parentClass的子类
         */
        fun isAssignable(parentClass: Class<*>): Boolean {
            if (source is MetadataReader) {
                return AssignableTypeFilter(parentClass).matches(this.source, metadataReaderFactory)
            }
            return ClassUtils.isAssignFrom(parentClass, source as Class<*>)
        }

        /**
         * 进行当前[SourceClass]的类加载
         * (某些特殊的操作下, 我们确实没有办法没有类的情况下去进行操作, 还是得借助类加载来做)
         *
         * @return 将当前[SourceClass]去加载得到的Class
         * @see ConfigurationClassParser.processImports
         */
        fun loadClass(): Class<*> {
            if (this.source is Class<*>) {
                return this.source
            }
            return ClassUtils.forName<Any>(
                (this.source as MetadataReader).classMetadata.getClassName(),
                resourceLoader.getClassLoader()
            )
        }

        /**
         * 获取当前的[SourceClass]上的全部直接标注的注解, 将注解也去转换成为一个[SourceClass]去进行包装
         *
         * @return 当前[SourceClass]上标注的注解列表
         */
        fun getAnnotations(): Collection<SourceClass> {
            val result = LinkedHashSet<SourceClass>()

            // 如果是Class的话, 那么直接getDeclaredAnnotations
            if (this.source is Class<*>) {
                for (annotation in source.declaredAnnotations) {
                    val annotationType = annotation.annotationClass.java
                    if (!annotationType.name.startsWith("java")) {
                        result += asSourceClass(annotationType, DEFAULT_EXCLUSION_FILTER)
                    }
                }

                // 如果当前不是Class的话, 那么使用MetadataReader去获取Metadata的注解信息...
            } else {
                for (annotationType in this.metadata.getAnnotationTypes()) {
                    if (!annotationType.startsWith("java")) {
                        result += asSourceClass(annotationType, DEFAULT_EXCLUSION_FILTER)
                    }
                }
            }

            return result
        }

        /**
         * 返回当前[SourceClass]的SuperClass(直接父类), 并包装成为[SourceClass],
         * source还是得以[MetadataReader]的方式去进行读取, 我们无法完成类加载.
         *
         * @return SourceClass of SuperClass
         */
        fun getSuperClass(): SourceClass {
            // 如果source就是Class的话, 那么我们直接使用superClass去构建SourceClass
            if (this.source is Class<*>) {
                return asSourceClass(this.source.superclass, DEFAULT_EXCLUSION_FILTER)
            }

            // 如果source是MetadataReader的话, 那么我们只有它的superClassName, 这里别去进行类加载
            // 我们也采用MetadataReader的方式去暴露SourceClass
            return asSourceClass(
                (source as MetadataReader).classMetadata.getSuperClassName()!!,
                DEFAULT_EXCLUSION_FILTER
            )
        }

        /**
         * 获取当前[SourceClass]的成员类列表
         *
         * @return [SourceClass]的成员类列表
         */
        fun getMemberClasses(): Collection<SourceClass> {
            var sourceToProcess = this.source

            // 如果是Class的话, 那么我们尝试去获取到所有的定义的类
            if (sourceToProcess is Class<*>) {
                val declaredClasses = sourceToProcess.declaredClasses
                try {
                    val members = ArrayList<SourceClass>(declaredClasses.size)
                    for (declaredClass in declaredClasses) {
                        members.add(asSourceClass(declaredClass, DEFAULT_EXCLUSION_FILTER))
                    }
                    return members // return members
                } catch (ex: NoClassDefFoundError) {
                    // 如果出现了成员类缺少依赖(链接)的情况, 我们尝试使用ASM去进行读取
                    sourceToProcess = metadataReaderFactory.getMetadataReader(sourceToProcess.name)
                }
            }
            sourceToProcess as MetadataReader

            // 如果使用Class读取失败, 或者根本不是一个Class的话, 那么需要使用MetadataReader, 去使用ASM的方式去读取成员类的信息
            val memberClassNames = sourceToProcess.classMetadata.getMemberClassNames()
            val members = ArrayList<SourceClass>(memberClassNames.size)
            for (memberClassName in memberClassNames) {
                try {
                    members += asSourceClass(memberClassName, DEFAULT_EXCLUSION_FILTER)
                } catch (ex: Exception) {
                    // 如果连使用ASM都无法解析到该类的话, 那么log输出一下
                    if (logger.isDebugEnabled) {
                        logger.debug("Failed to resolve member class [$memberClassName] - not considering it as a configuration class candidate")
                    }
                }

            }
            return members
        }

        /**
         * 获取当前[SourceClass]的直接接口列表, 并包装成为[SourceClass]
         *
         * @return 当前[SourceClass]的接口列表
         */
        fun getInterfaces(): Set<SourceClass> {
            val result = LinkedHashSet<SourceClass>()

            // 如果是Class的话, 可以直接读取接口列表
            if (source is Class<*>) {
                val interfaces = source.interfaces
                for (clazz in interfaces) {
                    result += asSourceClass(clazz, DEFAULT_EXCLUSION_FILTER)
                }

                // 如果是MetadataReader的话, 那么也还是得使用ASM的方式去进行获取所有的接口
            } else {
                for (interfaceName in metadata.getInterfaceNames()) {
                    result += asSourceClass(interfaceName, DEFAULT_EXCLUSION_FILTER)
                }
            }
            return result
        }

        /**
         * toString, 直接使用source去进行生成即可
         *
         * @return toString
         */
        override fun toString(): String = "$source"

        /**
         * equals, 采用[metadata]的类名去进行生成
         *
         * @param other other
         */
        override fun equals(@Nullable other: Any?): Boolean =
            this === other || other is SourceClass && other.metadata.getClassName() == this.metadata.getClassName()

        /**
         * hashCode, 采用[metadata]的类名去进行生成
         *
         * @return hashCode
         */
        override fun hashCode(): Int = metadata.getClassName().hashCode()
    }


    /**
     * ImportStack, 它是一个Import配置类的注册中心, 它维护了Import和被Import之间的配置类的关系;
     *
     * 同时, 它也是一个处理导入的栈(ArrayDeque), 如果出现了循环导入的情况, 通过它也可以去进行检测
     *
     * Note: 它会被注册到容器当中, 去支持ImportAware的解析, 因为有些被Import的配置类是需要获取到导入它的类的相关信息的
     */
    private class ImportStack : ImportRegistry, ArrayDeque<ConfigurationClass>() {

        /**
         * key-importedClass,value-导入importedClass该类的注解元信息
         */
        private val imports = LinkedHashMap<String, MutableList<AnnotationMetadata>>()

        /**
         * 给某个被导入的配置类, 注册导入它的那个类的元信息, 比如A导入了B, 那么importedClass=B, importingClass=A
         *
         * @param importedClass 被导入的配置类
         * @param importingClassMetadata 导入的类的注解元信息
         */
        fun registerImport(importedClass: String, importingClassMetadata: AnnotationMetadata) {
            imports.putIfAbsent(importedClass, LinkedList())
            imports[importedClass]!! += importingClassMetadata
        }

        /**
         * 给定importedClass, 去找到导入它的注解元信息
         *
         * @param importedClass importedClass
         * @return 导入importedClass的配置类的注解元信息, 如果找不到return null
         */
        override fun getImportingClassFor(importedClass: String): AnnotationMetadata? {
            val metadata = imports[importedClass]
            return if (metadata.isNullOrEmpty()) return null else metadata.last()
        }

        /**
         * 给定importingClass, 去移除掉它导入的所有的信息, 因为key-importedClass, value-importedClassMetadata;
         * 因此需要遍历所有的value, 去进行挨个检查className是否匹配importingClass, 如果匹配的话, 就remove掉
         *
         * @param importingClass Import配置类
         */
        override fun removeImportingClass(importingClass: String) {
            for (value in imports.values) {
                val iterator = value.iterator()
                while (iterator.hasNext()) {
                    if (iterator.next().getClassName() == importingClass) {
                        iterator.remove()
                    }
                }
            }
        }
    }


    /**
     * DeferredImportSelectorHolder, 维护了DeferredImportSelector以及对应的ConfigurationClass
     */
    private inner class DeferredImportSelectorHolder(
        val configClass: ConfigurationClass, val deferredImportSelector: DeferredImportSelector
    )

    /**
     * 这是一个DeferredImportSelector的分组的Handler
     */
    private inner class DeferredImportSelectorGroupingHandler {
        /**
         * key-分组, value-分组当中的DeferredSelector列表
         */
        private val groupings: MutableMap<Any, DeferredImportSelectorGrouping> = LinkedHashMap()

        /**
         * 根据AnnotationMetadata去获取到对应的ConfigurationClass的缓存
         */
        private val configurationClasses = LinkedHashMap<AnnotationMetadata, ConfigurationClass>()

        /**
         * 注册一个DeferredImportSelector到GroupingHandler当中, 交给GroupingHandler去进行处理
         *
         * @param holder 包装了ConfigurationClass和DeferredImportSelector的Holder
         */
        fun register(holder: DeferredImportSelectorHolder) {
            // 如果给定了Group, 那么使用你的Group, 否则将会使用默认的Group...
            val groupClass = holder.deferredImportSelector.getGroup()

            // key-GroupClass, Value-Grouping
            groupings.computeIfAbsent(groupClass ?: holder) {
                DeferredImportSelectorGrouping(createGroup(groupClass))
            }.add(holder)

            // 建立起来Metadata->ConfigClass的缓存
            this.configurationClasses[holder.configClass.metadata] = holder.configClass
        }

        /**
         * 创建Group实例对象
         *
         * @param groupType groupType
         * @return Group
         */
        private fun createGroup(@Nullable groupType: Class<out Group>?): Group {
            val groupClass = groupType ?: DefaultDeferredImportSelectorGroup::class.java
            return ParserStrategyUtils.instanceClass(groupClass, environment, registry, resourceLoader)
        }

        /**
         * 处理分组的导入, 遍历GroupingHandler当中的所有的已经注册的所有的分组, 去完成分组的导入
         */
        fun processGroupImports() {
            this.groupings.forEach { (_, grouping) ->
                // 遍历该分组下的所有的DeferredImportSelector列表, 去完成Selector的处理
                grouping.getImports().forEach { entry ->
                    val exclusionFilter = grouping.getCandidateFilter()

                    val configClass = configurationClasses[entry.metadata]!!
                    // 调用ConfigurationClassParser的processImports方法, 去使用正常的方式去地处理@Import注解
                    this@ConfigurationClassParser.processImports(
                        configClass, asSourceClass(configClass, exclusionFilter),
                        listOf(asSourceClass(entry.importClassName, exclusionFilter)),
                        exclusionFilter
                    )
                }
            }
        }
    }

    /**
     * 默认的DeferredImportSelectorGroup的实现
     */
    private class DefaultDeferredImportSelectorGroup : Group {

        /**
         * DeferredImportSelector要去进行导入的类
         */
        private val imports = ArrayList<Entry>()

        override fun process(metadata: AnnotationMetadata, selector: DeferredImportSelector) {
            val selectImports = selector.selectImports(metadata)
            for (importClassName in selectImports) {
                imports += Entry(metadata, importClassName)
            }
        }

        override fun selectImports(): Iterable<Entry> = this.imports
    }

    /**
     * 这是一个DeferredImportSelector的分组的抽象, 在它的内部维护了该分组下的DeferredImportSelector列表
     */
    private inner class DeferredImportSelectorGrouping(private val group: Group) {

        /**
         * DeferredSelectors
         */
        private val deferredImportSelectors = ArrayList<DeferredImportSelectorHolder>()

        /**
         * 往分组当中添加一个DeferredImportSelector
         */
        fun add(holder: DeferredImportSelectorHolder) {
            this.deferredImportSelectors += holder
        }

        /**
         * 获取所有要去进行导入的自动配置类的Entry
         *
         * @return 自动配置类的Entry列表
         */
        fun getImports(): Iterable<Entry> {
            for (selector in this.deferredImportSelectors) {
                this.group.process(selector.configClass.metadata, selector.deferredImportSelector)
            }
            return this.group.selectImports()
        }

        /**
         * 获取候选配置类的Filter断言, 将当前分组下的所有的[ImportSelector]的ExclusionFilter去进行merge
         *
         * @return 用于匹配匹配类的Filter断言
         */
        fun getCandidateFilter(): Predicate<String> {
            var mergedFilter: Predicate<String> = DEFAULT_EXCLUSION_FILTER

            for (deferredImportSelector in deferredImportSelectors) {
                val exclusionFilter = deferredImportSelector.deferredImportSelector.getExclusionFilter()
                if (exclusionFilter != null) {
                    mergedFilter = mergedFilter.or(exclusionFilter)
                }
            }
            return mergedFilter
        }
    }

    /**
     * 这是一个延时执行的的ImportSelector的处理器, 负责处理容器当中注册的DeferredImportSelector;
     * 它负责将一个DeferredImportSelector注册到DeferredImportSelectorGroupingHandler当中, 而DeferredImportSelectorGroupingHandler则对
     * 不同的DeferredImportSelectorGroupingHandler去进行分组(分组依据为DeferredImportSelector.getGroup)
     *
     * @see DeferredImportSelector.getGroup
     */
    private inner class DeferredImportSelectorHandler {

        /**
         * DeferredImportSelectorHolders
         */
        private val deferredImportSelectors = ArrayList<DeferredImportSelectorHolder>()

        /**
         * 将DeferredImportSelector包装成为DeferredImportSelectorHolder保存到列表当中
         *
         * @param configClass 导入该Selector的配置类
         * @param deferredImportSelector 要注册的Selector
         */
        fun add(configClass: ConfigurationClass, deferredImportSelector: DeferredImportSelector) {
            val holder = DeferredImportSelectorHolder(configClass, deferredImportSelector)
            this.deferredImportSelectors.add(holder)
        }

        /**
         * 处理已经注册的所有DeferredImportSelector, 将它转交给DeferredImportSelectorGroupingHandler去进行分组和处理;
         * 这里会处理所有的分组下的所有的DeferredImportSelector, 去完成将组件去进行批量导入到Spring容器当中
         *
         * @see DeferredImportSelectorGroupingHandler
         * @see DeferredImportSelectorGrouping
         */
        fun process() {
            // 创建GroupingHandler, 并将排序好的DeferredImportSelector全部都给注册到GroupingHandler当中
            val groupingHandler = DeferredImportSelectorGroupingHandler()
            deferredImportSelectors.sortWith(DEFERRED_IMPORT_SELECTOR_COMPARATOR)
            deferredImportSelectors.forEach(groupingHandler::register)

            // 交给GroupingHandler去进行分组ImportSelector的导入
            groupingHandler.processGroupImports()
        }
    }
}