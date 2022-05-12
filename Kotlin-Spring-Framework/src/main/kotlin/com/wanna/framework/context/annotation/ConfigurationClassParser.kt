package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotation.ConfigurationCondition.ConfigurationPhase
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.util.BeanUtils
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.ReflectionUtils
import org.springframework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.Method
import java.util.function.Predicate

/**
 * 这是一个配置类的解析器，用来扫描配置类相关的注解，将其注册到容器当中
 */
open class ConfigurationClassParser(
    private val registry: BeanDefinitionRegistry,
    private val environment: Environment,
    private val classLoader: ClassLoader,
    private val componentScanBeanNameGenerator: BeanNameGenerator
) {
    companion object {
        // DeferredImportSelectorHolder的比较器，因为对DeferredImportSelector包装了一层，因此需要包装一层
        private val DEFERRED_IMPORT_SELECTOR_COMPARATOR = Comparator<DeferredImportSelectorHolder> { o1, o2 ->
            AnnotationAwareOrderComparator.INSTANCE.compare(o1.deferredImportSelector, o2.deferredImportSelector)
        }

        // 默认的用来去进行排除的Filter
        private val DEFAULT_EXCLUSION_FILTER = Predicate<String> { it.startsWith("java.") }
    }

    // ComponentScan注解的解析器
    private val parser: ComponentScanAnnotationParser =
        ComponentScanAnnotationParser(registry, environment, classLoader, componentScanBeanNameGenerator)

    // 条件计算器，计算该Bean是否应该被导入到容器当中？
    private val conditionEvaluator = ConditionEvaluator(this.registry, this.environment)

    // 维护了扫描出来的ConfigurationClass的集合
    private val configurationClasses = LinkedHashMap<ConfigurationClass, ConfigurationClass>()

    // 这是一个要进行延时处理的ImportSelector列表，需要完成所有的配置类的解析之后，才去进行处理
    // **SpringBoot完成自动配置，就是通过DeferredImportSelector去完成的**
    private val deferredImportSelectorHandler = DeferredImportSelectorHandler()

    // Import栈
    private val importStack = ImportStack()

    /**
     * 获取导入被@Import配置类的信息栈
     */
    open fun getImportRegistry(): ImportRegistry = this.importStack

    /**
     * 获取解析完成的配置类列表
     */
    open fun getConfigurationClasses(): MutableSet<ConfigurationClass> = this.configurationClasses.keys

    /**
     * 解析容器中已经有的BeanDefinition当中的相关导入组件的配置类；一个BeanDefinitionHolder当中维护了beanDefinition和beanName信息
     *
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

    private fun processConfigurationClass(configurationClass: ConfigurationClass, filter: Predicate<String>) {
        // 如果条件计算器计算得知需要去进行pass掉，那么在这里直接pass掉，它要导入的所有组件都pass掉
        if (conditionEvaluator.shouldSkip(configurationClass.metadata, ConfigurationPhase.PARSE_CONFIGURATION)) {
            return
        }

        // 如果已经处理过了，那么return...
        if (configurationClasses.containsKey(configurationClass)) {
            return
        }

        // 将当前正在处理的配置类注册到已有的配置类当中
        configurationClasses[configurationClass] = configurationClass

        // 执行真正的配置类的处理工作，处理各种注解...
        doProcessConfigurationClass(configurationClass, filter)
    }

    /**
     * 真正地去进行处理一个配置类
     *
     * @param configurationClass 目标配置类
     * @param filter 用来去进行排除的Filter，符合filter的要求(filter.test==true)的将会被排除掉
     */
    private fun doProcessConfigurationClass(configurationClass: ConfigurationClass, filter: Predicate<String>) {
        // 处理PropertySource注解
        processPropertySources(configurationClass)

        // 处理ComponentScan注解
        processComponentScans(configurationClass)

        // 处理ImportSource注解
        processImportSources(configurationClass)

        // 处理Import注解
        processImports(configurationClass, getImportCandidates(configurationClass.configurationClass, filter), filter)

        // 处理Bean注解
        processBeanMethods(configurationClass)
    }

    /**
     * 处理@PropertySource注解
     */
    private fun processPropertySources(configurationClass: ConfigurationClass) {
        AnnotatedElementUtils.findAllMergedAnnotations(
            configurationClass.configurationClass, PropertySource::class.java
        ).forEach { propertySource ->
            // 加载得到的PropertySource
            val source =
                BeanUtils.instantiateClass(propertySource.factory.java).create("wanna", propertySource.locations)
        }
    }

    /**
     * 处理@Bean注解的标注的方法，将所有的@Bean方法加入到候选列表当中
     *
     * @param configurationClass 要处理的目标配置类
     */
    private fun processBeanMethods(configurationClass: ConfigurationClass) {
        // 构建Filter，过滤出还有@Bean的方法
        val filterBeans: (Method) -> Boolean = { it.getAnnotation(Bean::class.java) != null }

        // 构建，添加@Bean的方法回调
        val addBeanMethod: (Method) -> Unit = { configurationClass.addBeanMethod(BeanMethod(it, configurationClass)) }

        // 遍历该配置类当中的所有方法，去进行@Bean方法的检测
        ReflectionUtils.doWithMethods(configurationClass.configurationClass, addBeanMethod, filterBeans)
    }

    /**
     * 处理@ImportSource注解，这个注解的作用是为别的方式导入Bean提供支持；比如在注解版的IOC容器当中，去提供对XML配置文件的处理
     *
     * @see ImportSource
     * @see BeanDefinitionReader 如何导入组件？通过自定义BeanDefinitionReader的方式去进行导入组件
     */
    private fun processImportSources(configurationClass: ConfigurationClass) {
        AnnotatedElementUtils.findAllMergedAnnotations(
            configurationClass.configurationClass, ImportSource::class.java
        ).forEach { importSource ->
            importSource.locations.forEach { location ->
                configurationClass.addImportSource(location, importSource.reader.java)
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
     * @param configurationClass 标注@Import的配置类
     * @param importCandidates @Import导入的配置类列表
     * @param exclusionFilter 要进行排除的Filter
     */
    @Suppress("UNCHECKED_CAST")
    private fun processImports(
        configurationClass: ConfigurationClass,
        importCandidates: Collection<Class<*>>,
        exclusionFilter: Predicate<String>
    ) {
        // 如果没有找到候选的要进行Import的组件，那么直接return
        if (importCandidates.isEmpty()) {
            return
        }
        importCandidates.forEach { candidate ->
            // 如果它是一个ImportSelector
            if (ClassUtils.isAssignFrom(ImportSelector::class.java, candidate)) {
                val selector = ParserStrategyUtils.instanceClass<ImportSelector>(candidate, environment, registry)
                // 如果它是一个延时处理的ImportSelector，那么需要缓存起来，后续一起去进行处理
                if (selector is DeferredImportSelector) {
                    deferredImportSelectorHandler.add(configurationClass, selector)
                } else {

                    // 如果selector使用了排除的Filter
                    val selectorExclusionFilter = selector.getExclusionFilter()
                    var filterToUse = exclusionFilter
                    if (selectorExclusionFilter != null) {
                        filterToUse = filterToUse.or(selectorExclusionFilter)
                    }
                    val imports = selector.selectImports(configurationClass.metadata)
                    // 递归处理Import导入的Selector
                    processImports(configurationClass, getImportCandidates(imports, filterToUse), filterToUse)
                }
                // 如果它是一个ImportBeanDefinitionRegistrar
            } else if (ClassUtils.isAssignFrom(ImportBeanDefinitionRegistrar::class.java, candidate)) {
                // 实例化，并保存ImportBeanDefinitionRegistrar到configurationClass当中
                val registrar =
                    ParserStrategyUtils.instanceClass<ImportBeanDefinitionRegistrar>(candidate, environment, registry)
                // value为配置类中的相关的的注解信息，在后续去回调ImportBeanDefinitionRegistrar时会以参数的形式传递给调用方
                configurationClass.addRegistrar(registrar, configurationClass.metadata)
                // 如果只是导入了一个普通组件，需要把它当做一个配置类去进行递归处理
            } else {
                // 注册Import导入的配置类的信息(第一个参数是被导入的配置类名，第二个参数是导入它的配置类的注解信息)
                importStack.registerImport(candidate.name, configurationClass.metadata)

                // 构建被导入的配置类信息，beanName等ConfigurationClassBeanDefinitionReader.registerBeanDefinitionForImportedConfigurationClass生成
                val importedConfigurationClass = ConfigurationClass(candidate, null)
                importedConfigurationClass.setImportedBy(configurationClass)   // set importedBy
                processConfigurationClass(importedConfigurationClass, exclusionFilter)  // 把当前类当做配置类去进行递归
            }
        }
    }

    /**
     * 获取@Import注解的候选类
     */
    private fun getImportCandidates(imports: Array<String>, filter: Predicate<String>): Collection<Class<*>> {
        return imports.filter { !filter.test(it) }.map { ClassUtils.forName<Any>(it) }.toCollection(HashSet())
    }

    /**
     * 获取@Import中导入的组件列表
     *
     * @param clazz 目标配置类
     * @param filter 要去进行排除的Filter
     */
    private fun getImportCandidates(clazz: Class<*>, filter: Predicate<String>): Set<Class<*>> {
        val imports = AnnotatedElementUtils.findAllMergedAnnotations(clazz, Import::class.java)
        return AnnotationAttributesUtils.asAnnotationAttributesSet(imports)  // attributes(set)
            .mapNotNull { it.getClassArray("value") }  // (classArray)
            .flatMap { it.toList() }  // flatMap，将classArray摊开
            .filter { !filter.test(it.name) }  // 使用filter去进行过滤
            .toSet()  // 转为Set去进行return
    }

    /**
     * 处理目标配置类上@ComponentScan注解
     *
     * @param configurationClass 目标配置类
     */
    private fun processComponentScans(configurationClass: ConfigurationClass) {
        // 找到注解上的ComponentScan注解
        val componentScans = AnnotatedElementUtils.findAllMergedAnnotations(
            configurationClass.configurationClass, ComponentScan::class.java
        )
        // 如果有ComponentScan注解，并且条件计算器计算不应该跳过，那么才需要遍历所有的ComponentScan注解去进行处理
        if (componentScans.isNotEmpty() && !this.conditionEvaluator.shouldSkip(
                configurationClass.metadata, ConfigurationPhase.REGISTER_BEAN
            )
        ) {
            // 处理@ComponentScan注解，将符合条件的BeanDefinition，导入到容器当中
            // 并且应该将@ComponentScan扫描进来的BeanDefinition，通通当做一个配置类去进行解析，递归
            AnnotationAttributesUtils.asAnnotationAttributesSet(componentScans)
                .forEach { parse(parser.parse(ComponentScanMetadata(configurationClass, it))) }
        }
    }

    /**
     * ImportStack，它是一个Import配置类的注册中心，它维护了Import和被Import之间的配置类的关系；
     * Note: 它会被注册到容器当中，去支持ImportAware的解析，因为有些被Import的配置类是需要获取到导入它的类的相关信息的
     */
    private class ImportStack : ImportRegistry {
        private val imports = LinkedHashMap<String, MutableList<AnnotationMetadata>>()

        /**
         * 给某个被导入的配置类，注册导入它的那个类的元信息，比如A导入了B，那么importedClass=B，importingClass=A
         *
         * @param importedClass 被导入的配置类
         * @param importingClassMetadata 导入的类的元信息
         */
        fun registerImport(importedClass: String, importingClassMetadata: AnnotationMetadata) {
            imports.putIfAbsent(importedClass, ArrayList())
            imports[importedClass]!! += importingClassMetadata
        }

        override fun getImportingClassFor(importedClass: String): AnnotationMetadata? {
            val metadatas = imports[importedClass]
            return if (metadatas == null || metadatas.isEmpty()) return null else metadatas[0]
        }

        override fun removeImportingClass(importedClass: String) {
            imports -= importedClass
        }
    }


    /**
     * DeferredImportSelectorHolder，维护了DeferredImportSelector以及对应的ConfigurationClass
     */
    private inner class DeferredImportSelectorHolder(
        val configurationClass: ConfigurationClass, val deferredImportSelector: DeferredImportSelector
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
                    val configurationClass = it.configurationClass
                    // 调用ConfigurationClassParser的processImports方法，去使用正常的方式去地处理@Import注解
                    this@ConfigurationClassParser.processImports(configurationClass,
                        getImportCandidates(selector.selectImports(configurationClass.metadata)) { false }) { false }
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
         * @param configurationClass 导入该Selector的配置类
         * @param deferredImportSelector 要注册的Selector
         */
        fun add(configurationClass: ConfigurationClass, deferredImportSelector: DeferredImportSelector) {
            val holder = DeferredImportSelectorHolder(configurationClass, deferredImportSelector)
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