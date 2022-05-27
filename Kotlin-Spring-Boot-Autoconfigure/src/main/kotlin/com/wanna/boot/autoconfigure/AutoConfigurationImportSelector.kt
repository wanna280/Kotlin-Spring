package com.wanna.boot.autoconfigure

import com.wanna.boot.autoconfigure.EnableAutoConfiguration.Companion.ENABLED_OVERRIDE_PROPERTY
import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.context.annotation.DeferredImportSelector
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个完成自动配置的ImportSelector，作用是从SpringFactories当中去加载通过EnableAutoConfiguration导入的相关配置类，
 * 并使用AutoConfigurationImportFilter，去对要导入的配置类去进行批量的筛选，可以通过不读取字节码、直接读取文件的方式去进行批量筛选掉；
 * 对于一个Bean具体的要不要导入到Spring容器当中，得通过Condition/SpringBootCondition去进行条件匹配来决定。
 * 怎么筛选？不管是对于OnBean/OnClass的相关条件，都是通过Class.forName直接去判断它是否在当前的应用的依赖当中，只要不在那么就不符合
 *
 * @see EnableAutoConfiguration
 * @see DeferredImportSelector
 * @see ConfigurationClassFilter
 */
open class AutoConfigurationImportSelector : DeferredImportSelector, BeanClassLoaderAware, BeanFactoryAware,
    EnvironmentAware, Ordered {

    companion object {
        @JvmField
        val EMPTY_ENTRY = AutoConfigurationEntry(emptyList(), emptySet())
    }

    private lateinit var beanFactory: BeanFactory

    private lateinit var environment: Environment

    private lateinit var classLoader: ClassLoader

    private var order: Int = Ordered.ORDER_LOWEST - 1

    // 自动配置类的导入Filter
    private var configurationClassFilter: ConfigurationClassFilter? = null

    /**
     * 设置当前的ImportSelector应该所在的分组
     */
    override fun getGroup(): Class<out DeferredImportSelector.Group>? {
        return AutoConfigurationGroup::class.java
    }

    override fun selectImports(metadata: AnnotationMetadata): Array<String> {
        return AutoConfigurationSorter().sort(getAutoConfigurationEntry(metadata).configurations).toTypedArray()
    }

    /**
     * 获取自动配置信息的Entry，它的内部维护了要进行AutoConfiguration的配置类和排除掉的配置类的具体信息
     *
     * @see AutoConfigurationEntry.configurations
     * @see AutoConfigurationEntry.excludes
     * @param metadata 导入EnableAutoConfiguration的配置类的注解信息
     */
    protected open fun getAutoConfigurationEntry(metadata: AnnotationMetadata): AutoConfigurationEntry {
        // 检查是否开启了自动配置("spring.boot.enableautoconfiguration"配置)，如果没有开启自动配置的话，那么直接return即可
        if (!isEnabled(metadata)) {
            return EMPTY_ENTRY
        }

        // 从SpringFactories当中去加载到所有的EnableAutoConfiguration配置类的className列表
        var configurations = getCandidateConfigurations(metadata, null)

        // 对加载出来的自动配置类列表利用LinkedHashSet去进行去重
        configurations = removeDuplicates(configurations)

        // 从配置文件/注解配置信息当中去加载要排除的className列表
        val excludes = getExcludes(metadata, null)

        // 将要导入的配置类当中去移除掉所有的要进行移除的className
        configurations.removeAll(excludes)

        // 利用AutoConfigurationImportFilter，利用autoconfiguration-metadata配置文件去完成自动配置类的提前过滤
        configurations = getConfigurationClassFilter().filter(configurations)

        // 发布AutoConfigurationImportEvent，通知所有的监听器去处理该事件
        fireAutoConfigurationImportEvents(configurations, excludes)

        // 利用已经获取的configurations和excludes列表去构建AutoConfigurationEntry，去进行返回
        return AutoConfigurationEntry(configurations, excludes)
    }

    /**
     * 获取候选的配置类，从SpringFactories当中去加载到所有的EnableAutoConfiguration导入的自动配置类的className列表
     */
    protected open fun getCandidateConfigurations(
        metadata: AnnotationMetadata, attributes: AnnotationAttributes?
    ): MutableList<String> {
        return ArrayList(SpringFactoriesLoader.loadFactoryNames(getAnnotationClass()))
    }

    /**
     * 发布AutoConfigurationImportEvent事件，通知所有的监听器，容器当中的自动配置已经完成了
     */
    protected open fun fireAutoConfigurationImportEvents(configurations: MutableList<String>, excludes: Set<String>) {
        // 从SpringFactories当中去加载AutoConfigurationImportListener
        val listeners = SpringFactoriesLoader.loadFactories(AutoConfigurationImportListener::class.java)
        val event = AutoConfigurationImportEvent(this, configurations, excludes)

        // 通知所有的AutoConfigurationImportListener监听器，SpringBoot的自动配置已经完成了...
        listeners.forEach {
            invokeAwareMethods(it)  // invoke Aware Methods
            it.onAutoConfigurationImportEvent(event)
        }
    }



    /**
     * 将候选的Configuration配置类className列表去重
     */
    protected open fun removeDuplicates(configurations: MutableList<String>): MutableList<String> {
        return ArrayList(LinkedHashSet(configurations))
    }

    /**
     * 从注解和配置文件当中去获取到要进行排除的EnableAutoCondition的配置类
     */
    protected open fun getExcludes(
        metadata: AnnotationMetadata, attributes: AnnotationAttributes?
    ): MutableSet<String> {
        return HashSet()
    }

    /**
     * 获取ConfigurationClassFilter，利用SpringBoot的metadata配置文件的方式，提前去对自动配置类去进行过滤
     *
     * @return 维护了AutoConfigurationImportFilter列表(OnBeanCondition/OnClassCondition等)的ClassFilter
     */
    protected open fun getConfigurationClassFilter(): ConfigurationClassFilter {
        var configurationClassFilter = this.configurationClassFilter
        if (configurationClassFilter == null) {
            // 从SpringFactories当中去加载AutoConfigurationImportFilter列表，并创建ConfigurationClassFilter对象
            // 这里一般情况下，会加载到OnBeanCondition/OnClassCondition等AutoConfigurationImportFilter...
            val importFilters =
                SpringFactoriesLoader.loadFactories(AutoConfigurationImportFilter::class.java, classLoader)
            configurationClassFilter = ConfigurationClassFilter(classLoader, importFilters)
            this.configurationClassFilter = configurationClassFilter
        }
        return configurationClassFilter
    }

    /**
     * 是否要开启SpringBoot的自动配置，默认情况下开启，除非在配置文件当中将"spring.boot.enableautoconfiguration"设为了false
     */
    protected open fun isEnabled(metadata: AnnotationMetadata): Boolean {
        if (this::class.java == AutoConfigurationImportSelector::class.java) {
            // 如果从环境当中获取到属性值不为false，则说明要开启自动配置，return true(默认也是return true)
            return environment.getProperty(ENABLED_OVERRIDE_PROPERTY, Boolean::class.java, true)
        }
        return true
    }

    /**
     * 这是一个AutoConfiguration的配置类的列表
     *
     * @param configurations 维护了要导入了配置类列表
     * @param excludes 维护了要进行排除的配置类列表
     */
    data class AutoConfigurationEntry(val configurations: List<String>, val excludes: Set<String>)

    private class AutoConfigurationGroup : DeferredImportSelector.Group

    /**
     * 配置类的ClassFilter，对要排除的配置类去进行排除，内部组合了AutoConfigurationImportFilter的列表
     *
     * @param classLoader classLoader
     * @param filters AutoConfigurationImportFilters
     */
    inner class ConfigurationClassFilter(
        classLoader: ClassLoader, private val filters: MutableList<AutoConfigurationImportFilter>
    ) {
        // 从配置文件(META-INF/spring-autoconfigure-metadata.properties)当中加载AutoConfiguration的Metadata信息
        private var autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(classLoader)

        /**
         * 使用AutoConfigurationImportFilter去过滤掉所有的在metadata当中就已经不匹配的配置类
         *
         * @param configurations 候选的AutoConfiguration的配置类
         * @return 使用AutoConfigurationImportFilter去完成过滤之后的配置类列表
         */
        fun filter(configurations: List<String>): MutableList<String> {
            // 将AutoConfiguration配置类列表转换为Array<String?>，因为需要将某个位置的元素设置为null，需要使用?类型
            val candidates = ArrayList<String?>(configurations).toTypedArray()
            // 遍历所有的AutoConfigurationImportFilter去进行匹配，如果matches[index]=false，那么就将candidates[index]设置为null
            filters.forEach {
                invokeAwareMethods(it)  // invoke Aware Methods
                val matches = it.matches(candidates, autoConfigurationMetadata)
                for (index in matches.indices) {
                    if (!matches[index]) {
                        candidates[index] = null  // set to null
                    }
                }
            }
            // 过滤出来所有的notNull的元素，并转为List去进行return
            return candidates.filterNotNull().toMutableList()
        }
    }

    /**
     * 获取自动配置的注解，默认为EnableAutoConfiguration
     */
    protected open fun getAnnotationClass(): Class<*> {
        return EnableAutoConfiguration::class.java
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.classLoader = classLoader
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun getOrder(): Int {
        return this.order
    }

    /**
     * 对于一个Bean，如果必要的话，需要去执行Aware接口当中的方法
     */
    private fun invokeAwareMethods(instance: Any) {
        if (instance is BeanFactoryAware) {
            instance.setBeanFactory(this.beanFactory)
        }
        if (instance is EnvironmentAware) {
            instance.setEnvironment(this.environment)
        }
        if (instance is BeanClassLoaderAware) {
            instance.setBeanClassLoader(this.classLoader)
        }
    }
}
