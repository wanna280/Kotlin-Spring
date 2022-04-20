package com.wanna.framework.context.util

import com.wanna.framework.beans.annotations.Bean
import com.wanna.framework.beans.annotations.Import
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.*
import com.wanna.framework.context.annotations.*
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import org.springframework.core.annotation.AnnotatedElementUtils
import java.util.function.Predicate

/**
 * 这是一个配置类的解析器，用来扫描配置类相关的注解，将其注册到容器当中
 */
class ConfigurationClassParser(
    _registry: BeanDefinitionRegistry,
    _environment: Environment,
    _classLoader: ClassLoader,
    _componentScanBeanNameGenerator: BeanNameGenerator
) {

    // BeanDefinition的注册中心
    val registry: BeanDefinitionRegistry = _registry

    // 容器对象对应的环境对象
    val environment: Environment = _environment

    // 类加载器
    val classLoader: ClassLoader = _classLoader

    // componentScanBeanNameGenerator
    val componentScanBeanNameGenerator = _componentScanBeanNameGenerator

    // ComponentScan注解的解析器
    private val parser: ComponentScanAnnotationParser =
        ComponentScanAnnotationParser(_registry, _environment, _classLoader, _componentScanBeanNameGenerator)

    // 维护了扫描出来的ConfigurationClass的集合
    val configurationClasses = LinkedHashMap<ConfigurationClass, ConfigurationClass>()

    /**
     * 获取解析完成的配置类列表
     */
    fun getConfigurationClasses(): MutableSet<ConfigurationClass> {
        return configurationClasses.keys
    }

    fun parse(candidates: Collection<BeanDefinitionHolder>) {
        candidates.forEach {
            parse(it.beanDefinition, it.beanName)
        }
    }

    fun parse(beanDefinition: BeanDefinition, beanName: String) {
        processConfigurationClass(ConfigurationClass(beanDefinition, beanName)) {
            it.startsWith("java.")
        }
    }

    private fun processConfigurationClass(configurationClass: ConfigurationClass, filter: Predicate<String>) {
        // 如果已经处理过了，那么return...
        if (configurationClasses.containsKey(configurationClass)) {
            return
        }

        // 将当前正在处理的配置类注册到已有的配置类当中
        configurationClasses[configurationClass] = configurationClass

        // 执行真正的配置类的处理工作，处理各种注解...
        doProcessConfigurationClass(configurationClass, filter)
    }

    private fun doProcessConfigurationClass(configurationClass: ConfigurationClass, filter: Predicate<String>) {

        // 处理PropertySource注解
        processPropertySources(configurationClass)

        // 处理ComponentScan注解
        processComponentScans(configurationClass)

        // 处理ImportSource注解
        processImportSources(configurationClass)

        // 处理Import注解
        processImports(configurationClass, getImportCandidates(configurationClass, filter), filter)

        // 处理Bean注解
        processBeanMethods(configurationClass)
    }

    /**
     * 处理PropertySource注解
     */
    private fun processPropertySources(configurationClass: ConfigurationClass) {
        AnnotatedElementUtils.findAllMergedAnnotations(
            configurationClass.configurationClass, PropertySource::class.java
        ).forEach { propertySource ->

            // 加载得到的PropertySource
            val source = ClassUtils.newInstance(propertySource.factory.java).create("wanna", propertySource.locations)
        }
    }

    /**
     * 处理Bean注解的方法，将所有的@Bean方法加入到候选列表当中
     */
    private fun processBeanMethods(configurationClass: ConfigurationClass) {
        ReflectionUtils.doWithMethods(configurationClass.configurationClass, { method ->
            configurationClass.addBeanMethod(BeanMethod(method))
        }, { method -> method.getAnnotation(Bean::class.java) != null })
    }

    /**
     * 处理ImportSource注解
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
     */
    private fun processImports(
        configurationClass: ConfigurationClass, importCandidates: Collection<Class<*>>, filter: Predicate<String>
    ) {
        if (importCandidates.isEmpty()) {
            return
        }
        importCandidates.forEach { candidate ->
            // 如果它是一个ImportSelector
            if (ClassUtils.isAssginFrom(ImportSelector::class.java, candidate)) {
                val importSelector = ClassUtils.newInstance(candidate) as ImportSelector
                // 如果它是一个延时的Selector
                if (importSelector is DeferredImportSelector) {

                } else {
                    val imports = importSelector.selectImports()
                    // 递归处理Import导入的Selector
                    processImports(configurationClass, getImportCandidates(imports, filter), filter)
                }
                // 如果它是一个ImportBeanDefinitionRegistrar
            } else if (ClassUtils.isAssginFrom(ImportBeanDefinitionRegistrar::class.java, candidate)) {

                // 实例化，并保存ImportBeanDefinitionRegistrar到configurationClass当中
                val registrar = ClassUtils.newInstance(candidate) as ImportBeanDefinitionRegistrar
                configurationClass.addRegistrar(registrar)

                // 如果只是导入了一个普通组件，需要把它当做一个配置类去进行递归处理
            } else {
                val importConfigurationClass = ConfigurationClass(candidate, null)
                importConfigurationClass.setImportedBy(configurationClass)   // importedBy
                processConfigurationClass(importConfigurationClass, filter)  // 把当前类当做配置类去进行递归
            }
        }
    }

    private fun getImportCandidates(imports: Array<String>, filter: Predicate<String>): Collection<Class<*>> {
        val set = HashSet<Class<*>>()
        imports.filter { !filter.test(it) }.forEach { set.add(ClassUtils.forName<Any>(it)) }
        return set
    }

    /**
     * 获取Import中导入的组件列表
     */
    private fun getImportCandidates(configurationClass: ConfigurationClass, filter: Predicate<String>): Set<Class<*>> {
        val set = HashSet<Class<*>>()
        val imports =
            AnnotatedElementUtils.findAllMergedAnnotations(configurationClass.configurationClass, Import::class.java)
        val attributesSet = AnnotationAttributesUtils.asAnnotationAttributesSet(imports)
        attributesSet.forEach { attr ->
            val classes = attr!!.getClassArray("value")
            classes!!.forEach { clazz ->
                set.add(clazz)
            }
        }
        return set
    }

    private fun processComponentScans(configurationClass: ConfigurationClass) {
        // 找到注解上的ComponentScan注解
        val componentScans = AnnotatedElementUtils.findAllMergedAnnotations(
            configurationClass.configurationClass, ComponentScan::class.java
        )
        val attributesSet = AnnotationAttributesUtils.asAnnotationAttributesSet(componentScans)
        // 遍历标注的所有CompoentScan注解
        attributesSet.forEach { attr ->
            if (componentScans.isNotEmpty()) {
                // ComponentScan
                val beanDefinitions = parser.parse(ComponentScanMetadata(configurationClass, attr!!))
            }
        }


    }
}