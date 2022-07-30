package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotation.ConfigurationCondition.ConfigurationPhase
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.environment.CompositePropertySource
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.support.DefaultPropertySourceFactory
import com.wanna.framework.core.io.support.PropertySourceFactory
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.util.BeanUtils
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.StringUtils
import org.slf4j.LoggerFactory
import java.util.LinkedList
import java.util.function.Predicate

/**
 * 这是一个配置类的解析器，用来扫描配置类相关的注解，将其注册到容器当中
 */
open class ConfigurationClassParser(
    private val registry: BeanDefinitionRegistry,
    private val environment: Environment,
    classLoader: ClassLoader,
    componentScanBeanNameGenerator: BeanNameGenerator
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ConfigurationClass::class.java)

        // DeferredImportSelectorHolder的比较器，因为对DeferredImportSelector包装了一层，因此需要包装一层
        private val DEFERRED_IMPORT_SELECTOR_COMPARATOR = Comparator<DeferredImportSelectorHolder> { o1, o2 ->
            AnnotationAwareOrderComparator.INSTANCE.compare(o1.deferredImportSelector, o2.deferredImportSelector)
        }

        // 默认的用来去进行排除的Filter
        private val DEFAULT_EXCLUSION_FILTER =
            Predicate<String> { it.startsWith("java.") || it.startsWith("com.wanna.framework.context.stereotype.") }
    }

    // ComponentScan注解的解析器
    private val parser: ComponentScanAnnotationParser =
        ComponentScanAnnotationParser(registry, environment, classLoader, componentScanBeanNameGenerator)

    // 条件计算器，计算该Bean是否应该被导入到容器当中？
    private val conditionEvaluator = ConditionEvaluator(this.registry, this.environment)

    // 维护了扫描出来的ConfigurationClass的集合
    private val configClasses = LinkedHashMap<ConfigurationClass, ConfigurationClass>()

    // 这是一个要进行延时处理的ImportSelector列表，需要完成所有的配置类的解析之后，才去进行处理
    // **SpringBoot完成自动配置，就是通过DeferredImportSelector去完成的**
    private val deferredImportSelectorHandler = DeferredImportSelectorHandler()

    // Import栈，一方面注册importedClass与导入它的配置类的元信息，一方面记录Import过程当中的栈轨迹(判断是否发生了循环导入)
    private val importStack = ImportStack()

    /**
     * 获取导入被@Import配置类的信息的注册中心(导入栈)，用来处理ImportAware接口的注入Metadata信息
     */
    open fun getImportRegistry(): ImportRegistry = this.importStack

    /**
     * 获取解析完成的配置类列表
     */
    open fun getConfigurationClasses(): Set<ConfigurationClass> = this.configClasses.keys

    /**
     * 解析容器中已经有的BeanDefinition当中的相关导入组件的配置类；
     * 一个BeanDefinitionHolder当中维护了beanDefinition和beanName信息
     *
     * @param candidates 候选的BeanDefinitionHolder
     * @see BeanDefinitionHolder
     */
    open fun parse(candidates: Collection<BeanDefinitionHolder>) {
        candidates.forEach { parse(it.beanDefinition, it.beanName) }

        // 在处理完所有的应该扫描的相关配置类之后，应该去进行处理延时加载的ImportSelector
        // 比如SpringBoot的自动装配，就会在这里去完成，它的执行时期，比普通的Bean的处理更晚
        deferredImportSelectorHandler.process()
    }

    /**
     * 针对指定的BeanDefinition，把它作为配置类，去进行配置类的处理
     *
     * @param beanDefinition 指定的beanDefinition
     * @param beanName beanName
     */
    open fun parse(beanDefinition: BeanDefinition, beanName: String) {
        processConfigurationClass(ConfigurationClass(beanDefinition, beanName), DEFAULT_EXCLUSION_FILTER)
    }

    /**
     * 给定指定的beanClass和beanName，去构建配置类，并去进行处理
     *
     * @param beanClass beanClass
     * @param beanName beanName
     */
    open fun parse(beanClass: Class<*>, beanName: String) {
        processConfigurationClass(ConfigurationClass(beanClass, beanName), DEFAULT_EXCLUSION_FILTER)
    }

    private fun asSourceClass(configClass: ConfigurationClass): SourceClass {
        return SourceClass(configClass.configurationClass)
    }

    /**
     * 解析配置类
     *
     * @param configClass 目标配置类
     * @param filter 去进行排除掉的filter
     */
    private fun processConfigurationClass(configClass: ConfigurationClass, filter: Predicate<String>) {
        // 如果条件计算器计算得知需要去进行pass掉，那么在这里直接pass掉，它要导入的所有组件都pass掉
        if (conditionEvaluator.shouldSkip(configClass.metadata, ConfigurationPhase.PARSE_CONFIGURATION)) {
            return
        }

        // 如果已经处理过了，那么return...
        if (configClasses.containsKey(configClass)) {
            return
        }

        // 将当前正在处理的配置类注册到已有的配置类当中，避免出现StackOverflow的情况...
        configClasses[configClass] = configClass

        // 把ConfigurationClass转为sourceClass去进行匹配...sourceClass有可能为它的父类这种情况...
        // 这里需要递归处理它的所有父类，都当做配置类去进行处理，但是它的父类当中的所有BeanMethod、ImportSource、ImportBeanDefinitionRegistrar都保存到configClass当中
        var sourceClass: SourceClass? = asSourceClass(configClass)  // 最开始，把configClass当做sourceClass即可
        do {
            sourceClass = doProcessConfigurationClass(configClass, sourceClass!!, filter)
        } while (sourceClass != null)
    }

    /**
     * 交给这个方法，去进行真正地去处理一个配置类
     *
     * @param configClass 目标配置类(用于存放BeanMethod/ImportSource/ImportBeanDefinitionRegistrar/PropertySource等)，不会进行匹配的操作
     * @param sourceClass 源类(有可能它目标配置类的父类的情况...)，用来完成所有的匹配工作
     * @param filter 用来去进行排除的Filter，符合filter的要求(filter.test==true)的将会被排除掉
     * @return 如果有父类的话，return 父类；如果没有父类的话，return null
     */
    private fun doProcessConfigurationClass(
        configClass: ConfigurationClass, sourceClass: SourceClass, filter: Predicate<String>
    ): SourceClass? {
        // Note: 如果该配置类有@Component注解，那么它有资格去处理内部类
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
        processImports(configClass, sourceClass, asSourceClasses(configClass.configurationClass, filter), filter)

        // 处理@Bean注解
        processBeanMethods(configClass, sourceClass)

        val superclass: Class<*>? = sourceClass.clazz.superclass

        // 如果还有superClass，那么return superClass
        if (superclass != null && !superclass.name.startsWith("java.")) {
            return SourceClass(superclass)
        }
        return null
    }

    /**
     * 处理每个配置类的内部的成员类
     *
     * @param configClass 目标配置类
     * @param sourceClass 源类(有可能为目标配置类的父类)
     * @param filter 排除的filter
     */
    private fun processMemberClasses(
        configClass: ConfigurationClass, sourceClass: SourceClass, filter: Predicate<String>
    ) {
        val clazz = sourceClass.clazz
        val candidates = LinkedHashSet<SourceClass>()
        // 遍历所有的内部类，去进行匹配...
        for (declaredClass in clazz.declaredClasses) {
            if (ConfigurationClassUtils.isConfigurationCandidate(AnnotationMetadata.introspect(declaredClass))) {
                candidates += SourceClass(declaredClass)
            }
        }


        // 遍历所有的内部类，去进行递归处理...设置ImportedBy，beanName将会在后期Reader当中去进行生成(内部类其实和@Import导入的完全等效啊...)
        candidates.forEach {
            if (importStack.contains(configClass)) {
                logger.info("[${configClass}]出现了循环导入的情况...")
            } else {
                importStack.push(configClass)
                try {
                    // 根据内部类，去构建一个ConfigurationClass，设置importedBy为外部类...(它被外部类所导入)
                    processConfigurationClass(it.asConfigClass(configClass), filter)  // 把成员类当做配置类去递归处理...
                } finally {
                    importStack.pop()
                }
            }
        }
    }

    /**
     * 处理某个配置类上的@PropertySource注解，将该PropertySource导入的locations当中的资源，添加到Spring Environment当中
     *
     * @param configClass 要去进行匹配的目标配置类
     * @param sourceClass 源类
     */
    private fun processPropertySources(configClass: ConfigurationClass, sourceClass: SourceClass) {
        val propertySources =
            AnnotatedElementUtils.findAllMergedAnnotations(sourceClass.clazz, PropertySource::class.java)
        propertySources.forEach { propertySource ->
            val name = if (!StringUtils.hasText(propertySource.name)) null else propertySource.name
            val locations = propertySource.locations

            // 创建PropertySourceFactory，交给它去完成配置文件(Properties)的加载工作...
            var factoryClass = propertySource.factory.java
            if (factoryClass == PropertySourceFactory::class.java) {
                factoryClass = DefaultPropertySourceFactory::class.java
            }
            val propertySourceFactory = BeanUtils.instantiateClass(factoryClass)

            // 遍历给定的所有资源的位置(location)，使用PropertySourceFactory去进行加载
            locations.forEach {
                // location支持使用占位符
                val location = this.environment.resolveRequiredPlaceholders(it)
                addPropertySource(propertySourceFactory.createPropertySource(name, location))
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

            // 如果之前就是CompositePropertySource，那么直接添加到之前的后面就行
            if (oldPropertySource is CompositePropertySource) {
                oldPropertySource.addPropertySource(propertySource)

                // 如果之前还不是组合的PropertySource，那么需要组合旧的和新的
            } else {
                val composite = CompositePropertySource(name)
                composite.addPropertySource(oldPropertySource)
                composite.addPropertySource(propertySource)
                propertySources.replace(name, composite)  // replace
            }

            // 如果之前都还没存在过该name的PropertySource，直接addLast到Environment当中
        } else {
            propertySources.addLast(propertySource)
        }
    }

    /**
     * 处理@Bean注解的标注的方法，将所有的@Bean方法加入到候选列表当中
     *
     * @param configClass 要处理的目标配置类
     */
    private fun processBeanMethods(configClass: ConfigurationClass, sourceClass: SourceClass) {
        val beanMethods = sourceClass.metadata.getAnnotatedMethods(Bean::class.java.name)
        beanMethods.forEach {
            configClass.addBeanMethod(BeanMethod(it, configClass))
        }
    }

    /**
     * 处理@ImportSource注解，这个注解的作用是为别的方式导入Bean提供支持；比如在注解版的IOC容器当中，去提供对XML配置文件的处理
     *
     * @see ImportSource
     * @see BeanDefinitionReader 如何导入组件？通过自定义BeanDefinitionReader的方式去进行导入组件
     */
    private fun processImportSources(configClass: ConfigurationClass, sourceClass: SourceClass) {
        AnnotatedElementUtils.findAllMergedAnnotations(sourceClass.clazz, ImportSource::class.java)
            .forEach { importSource ->
                importSource.locations.forEach { location ->
                    configClass.addImportSource(location, importSource.reader.java)
                }
            }
    }

    /**
     * 处理Import注解
     * (1)如果@Import导入的是一个ImportSelector，那么把它的selectImports方法返回的组件当做候选的Import组件去进行继续处理
     * ----如果是一个DeferredImportSelector，那么把它保存到DeferredImportSelectorHandler当中，等待配置类处理完之后再去进行处理
     * (2)如果@Import导入的是一个ImportBeanDefinitionRegistrar，那么需要把它保存到配置类当中，等待后续回调
     * (3)如果@Import导入的是一个普通的组件，那么把它当做一个普通的配置类去进行递归处理
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
        // 如果没有找到候选的要进行Import的组件，那么直接return
        if (importCandidates.isEmpty()) {
            return
        }
        importStack.push(configClass)  // push stack
        try {
            importCandidates.forEach { candidate ->
                // 如果它是一个ImportSelector(不会注册到容器当中)
                if (candidate.isAssignable(ImportSelector::class.java)) {
                    val selector =
                        ParserStrategyUtils.instanceClass<ImportSelector>(candidate.clazz, environment, registry)
                    // 如果它是一个延时处理的ImportSelector，那么需要缓存起来，后续一起去进行处理
                    if (selector is DeferredImportSelector) {
                        deferredImportSelectorHandler.add(configClass, selector)
                    } else {
                        // 如果selector使用了排除的Filter，那么需要将它与exclusionFilter进行或运算
                        // 表示只要其中一个符合，那么就不匹配...
                        val selectorExclusionFilter = selector.getExclusionFilter()
                        var filterToUse = exclusionFilter
                        if (selectorExclusionFilter != null) {
                            filterToUse = filterToUse.or(selectorExclusionFilter)
                        }
                        val imports = selector.selectImports(currentSourceClass.metadata)
                        val sourceClasses = asSourceClasses(imports, filterToUse)
                        // 递归处理Import导入的Selector
                        processImports(configClass, currentSourceClass, sourceClasses, filterToUse)
                    }
                    // 如果它是一个ImportBeanDefinitionRegistrar(不会注册到容器当中)
                } else if (candidate.isAssignable(ImportBeanDefinitionRegistrar::class.java)) {
                    // 实例化，并保存ImportBeanDefinitionRegistrar到configClass当中
                    val registrar = ParserStrategyUtils.instanceClass<ImportBeanDefinitionRegistrar>(
                        candidate.clazz, environment, registry
                    )
                    // value为配置类中的相关的的注解信息，在后续去回调ImportBeanDefinitionRegistrar时会以参数的形式传递给调用方
                    configClass.addRegistrar(registrar, currentSourceClass.metadata)
                    // 如果只是导入了一个普通组件，需要把它当做一个配置类去进行递归处理
                } else {
                    // 注册Import导入的配置类的信息(第一个参数是被导入的配置类名(key)，第二个参数是导入它的配置类的注解信息(value))
                    importStack.registerImport(candidate.clazz.name, currentSourceClass.metadata)
                    // 构建被导入的配置类信息，beanName等ConfigurationClassBeanDefinitionReader.registerBeanDefinitionForImportedConfigurationClass生成
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
        return imports.filter { !exclusionFilter.test(it) }  // 丢掉不合法的
            .map { SourceClass(ClassUtils.forName<Any>(it)) }  // 转为className转为SourceClass
            .toList()
    }

    /**
     * 获取@Import中导入的组件列表
     *
     * @param clazz 目标配置类
     * @param filter 要去进行排除的Filter
     * @return @Import导入的配置类列表
     */
    private fun asSourceClasses(clazz: Class<*>, filter: Predicate<String>): Set<SourceClass> {
        val imports = AnnotatedElementUtils.findAllMergedAnnotations(clazz, Import::class.java)
        return AnnotationAttributesUtils.asAnnotationAttributesSet(imports)  // attributes(set)
            .map { it.getClassArray("value") }  // (classArray)
            .flatMap { it.toList() }  // flatMap，将classArray摊开
            .filter { !filter.test(it.name) }  // 使用filter去进行过滤
            .map { SourceClass(it) }  // 转为sourceClass
            .toSet()  // 转为Set去进行return
    }

    /**
     * 处理sourceClass配置类上@ComponentScan注解
     *
     * @param sourceClass 要寻找@ComponentScan注解的源类
     */
    private fun processComponentScans(sourceClass: SourceClass) {
        // 找到注解上的ComponentScan注解
        val componentScans =
            AnnotatedElementUtils.findAllMergedAnnotations(sourceClass.clazz, ComponentScan::class.java)
        if (componentScans.isEmpty()) {
            return
        }
        // 如果有@ComponentScan注解，并且条件计算器计算不应该跳过，那么才需要遍历所有的ComponentScan注解去进行处理
        if (!this.conditionEvaluator.shouldSkip(sourceClass.metadata, ConfigurationPhase.REGISTER_BEAN)) {
            componentScans.forEach {
                // 处理@ComponentScan注解，将符合条件的BeanDefinition，导入到容器当中
                // 并且应该将@ComponentScan扫描进来的BeanDefinition，通通当做一个配置类去进行解析，递归
                val attributes = AnnotationAttributesUtils.asNonNullAnnotationAttributes(it)
                parse(parser.parse(attributes, sourceClass.metadata.getClassName()))
            }
        }
    }

    /**
     * 描述了要去进行解析的一个配置类的相关信息，对于一个配置类来说，可能会存在有多个父类，
     * 对于它以及它的所有的父类，都应该当做一个SourceClass去进行处理
     *
     * @param clazz 要去进行描述的配置类
     */
    private class SourceClass(val clazz: Class<*>) {
        val metadata = AnnotationMetadata.introspect(clazz)

        /**
         * 将SourceClass转换为ConfigurationClass(配置类对象)
         *
         * @param importedBy 它是被哪个类导入进来的？
         * @return 构建好的ConfigurationClass
         */
        fun asConfigClass(importedBy: ConfigurationClass): ConfigurationClass {
            val configClass = ConfigurationClass(clazz)
            configClass.setImportedBy(importedBy)
            return configClass
        }

        /**
         * 判断它是否和某个类型匹配？
         *
         * @param parentClass 父类
         * @return 当前clazz是否是parentClass的子类
         */
        fun isAssignable(parentClass: Class<*>): Boolean {
            return ClassUtils.isAssignFrom(parentClass, clazz)
        }

        override fun toString(): String = "SourceClass(clazz=$clazz)"
    }


    /**
     * ImportStack，它是一个Import配置类的注册中心，它维护了Import和被Import之间的配置类的关系；
     *
     * 同时，它也是一个处理导入的栈(ArrayDeque)，如果出现了循环导入的情况，通过它也可以去进行检测
     *
     * Note: 它会被注册到容器当中，去支持ImportAware的解析，因为有些被Import的配置类是需要获取到导入它的类的相关信息的
     */
    private class ImportStack : ImportRegistry, java.util.ArrayDeque<ConfigurationClass>() {

        /**
         * key-importedClass,value-导入importedClass该类的注解元信息
         */
        private val imports = LinkedHashMap<String, MutableList<AnnotationMetadata>>()

        /**
         * 给某个被导入的配置类，注册导入它的那个类的元信息，比如A导入了B，那么importedClass=B，importingClass=A
         *
         * @param importedClass 被导入的配置类
         * @param importingClassMetadata 导入的类的注解元信息
         */
        fun registerImport(importedClass: String, importingClassMetadata: AnnotationMetadata) {
            imports.putIfAbsent(importedClass, LinkedList())
            imports[importedClass]!! += importingClassMetadata
        }

        /**
         * 给定importedClass，去找到导入它的注解元信息
         *
         * @param importedClass importedClass
         * @return 导入importedClass的配置类的注解元信息，如果找不到return null
         */
        override fun getImportingClassFor(importedClass: String): AnnotationMetadata? {
            val metadatas = imports[importedClass]
            return if (metadatas == null || metadatas.isEmpty()) return null else metadatas.last()
        }

        /**
         * 给定importingClass，去移除掉它导入的所有的信息，因为key-importedClass，value-importedClassMetadata；
         * 因此需要遍历所有的value，去进行挨个检查className是否匹配importingClass，如果匹配的话，就remove掉
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
     * DeferredImportSelectorHolder，维护了DeferredImportSelector以及对应的ConfigurationClass
     */
    private inner class DeferredImportSelectorHolder(
        val configClass: ConfigurationClass, val deferredImportSelector: DeferredImportSelector
    )

    /**
     * 这是一个DeferredImportSelector的分组的Handler
     */
    private inner class DeferredImportSelectorGroupingHandler {
        // key-分组，value-分组当中的DeferredSelector列表
        private val groupings: MutableMap<Any, DeferredImportSelectorGrouping> = HashMap()

        /**
         * 注册一个DeferredImportSelector到GroupingHandler当中，交给GroupingHandler去进行处理
         *
         * @param holder 包装了ConfigurationClass和DeferredImportSelector的Holder
         */
        fun register(holder: DeferredImportSelectorHolder) {
            val groupClass = holder.deferredImportSelector.getGroup() ?: DeferredImportSelector.Group::class.java
            groupings.putIfAbsent(groupClass, DeferredImportSelectorGrouping())
            groupings[groupClass]?.add(holder)
        }

        /**
         * 处理分组的导入，遍历GroupingHandler当中的所有的已经注册的所有的分组，去完成分组的导入
         */
        fun processGroupImports() {
            groupings.forEach { (_, grouping) ->
                // 遍历该分组下的所有的DeferredImportSelector列表，去完成Selector的处理
                grouping.getImports().forEach {
                    val selector = it.deferredImportSelector
                    val configClass = it.configClass
                    // 调用ConfigurationClassParser的processImports方法，去使用正常的方式去地处理@Import注解
                    this@ConfigurationClassParser.processImports(configClass,
                        asSourceClass(configClass),
                        asSourceClasses(selector.selectImports(configClass.metadata)) { false }) { false }
                }
            }
        }
    }

    /**
     * 这是一个DeferredImportSelector的分组的抽象，在它的内部维护了该分组下的DeferredImportSelector列表
     */
    private inner class DeferredImportSelectorGrouping {
        private val deferredImportSelectors = ArrayList<DeferredImportSelectorHolder>()

        /**
         * 往分组当中添加一个DeferredImportSelector
         */
        fun add(holder: DeferredImportSelectorHolder) {
            this.deferredImportSelectors += holder
        }

        /**
         * 获取该分组下已经注册的DeferredImportSelector列表
         */
        fun getImports(): List<DeferredImportSelectorHolder> {
            return this.deferredImportSelectors
        }
    }

    /**
     * 这是一个延时执行的的ImportSelector的处理器，负责处理容器当中注册的DeferredImportSelector；
     * 它负责将一个DeferredImportSelector注册到DeferredImportSelectorGroupingHandler当中，而DeferredImportSelectorGroupingHandler则对
     * 不同的DeferredImportSelectorGroupingHandler去进行分组(分组依据为DeferredImportSelector.getGroup)
     *
     * @see DeferredImportSelector.getGroup
     */
    private inner class DeferredImportSelectorHandler {
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
         * 处理已经注册的所有DeferredImportSelector，将它转交给DeferredImportSelectorGroupingHandler去进行分组和处理；
         * 这里会处理所有的分组下的所有的DeferredImportSelector，去完成将组件去进行批量导入到Spring容器当中
         *
         * @see DeferredImportSelectorGroupingHandler
         * @see DeferredImportSelectorGrouping
         */
        fun process() {
            // 创建GroupingHandler，并将排序好的DeferredImportSelector全部都给注册到GroupingHandler当中
            val groupingHandler = DeferredImportSelectorGroupingHandler()
            deferredImportSelectors.sortWith(DEFERRED_IMPORT_SELECTOR_COMPARATOR)
            deferredImportSelectors.forEach(groupingHandler::register)

            // 交给GroupingHandler去进行分组ImportSelector的导入
            groupingHandler.processGroupImports()
        }
    }
}