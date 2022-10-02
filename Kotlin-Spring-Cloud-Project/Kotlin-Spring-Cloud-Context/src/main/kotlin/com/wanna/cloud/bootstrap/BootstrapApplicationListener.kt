package com.wanna.cloud.bootstrap

import com.wanna.boot.ApplicationContextInitializer
import com.wanna.boot.ApplicationType
import com.wanna.boot.Banner
import com.wanna.boot.SpringApplication
import com.wanna.boot.builder.SpringApplicationBuilder
import com.wanna.boot.context.event.ApplicationEnvironmentPreparedEvent
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.environment.*
import com.wanna.framework.util.StringUtils

/**
 * 这是一个处理Bootstrap的ApplicationListener，监听SpringApplication的ApplicationEnvironmentPreparedEvent事件，并进行处理；
 * 通过构建一个Bootstrap容器的方式，将SpringCloud的bootstrap当中的配置文件等相关信息，一并去合并到Root SpringApplication当中
 *
 * @see ApplicationEnvironmentPreparedEvent
 */
open class BootstrapApplicationListener : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    companion object {
        const val BOOTSTRAP_PROPERTY_SOURCE_NAME = "bootstrap"
        const val DEFAULT_PROPERTIES = "springCloudDefaultProperties"
    }

    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val environment = event.environment
        // 如果Bootstrap标识位为false，那么不启用Bootstrap....(在新版的SpringCloud当中，也会同时使用标识类Marker去进行判断，没有Marker也会不生效...)
        val enabled = environment.getProperty("spring.cloud.bootstrap.enabled", Boolean::class.java, true)
        if (!enabled) {
            return
        }

        // 通过判断已经存在BootstrapPropertySource，从而判断当前是个Bootstrap容器，就别继续进行构建了，不然会出现StackOverFlow
        if (environment.getPropertySources().contains(BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            return
        }

        // 从Root Environment当中去解析Bootstrap容器的配置文件名，默认值为bootstrap
        val configName = environment.resolvePlaceholders("%{spring.cloud.bootstrap.name:bootstrap}")!!

        // 运行Bootstrap容器，并将得到的环境和Root的环境去进行合并...
        val context = bootstrapServiceContext(environment, event.application, configName)

        // apply，将Bootstrap当中的相关ApplicationContextInitializer应用给Root容器当中...
        apply(context, event.application, environment)
    }

    private fun apply(
        context: ConfigurableApplicationContext, application: SpringApplication, environment: ConfigurableEnvironment
    ) {
        if (application.getAllSources().contains(BootstrapMarkerConfiguration::class.java)) {
            return
        }
        application.addPrimarySource(BootstrapMarkerConfiguration::class.java) // 标识Bootstrap已经处理过了

        // 将Bootstrap容器当中的Initializer全部merge到Root Application当中，实现将Bootstrap当中的内容转移到Root当中...
        // 其中包含PropertySourceBootstrapConfiguration
        val initializers = LinkedHashSet(application.getInitializers())
        initializers.addAll(getOrderedBeansForType(context, ApplicationContextInitializer::class.java))
        application.setInitializers(initializers)
    }

    /**
     * 引导Bootstrap的ApplicationContext
     *
     * @param environment 发布ApplicationEnvironmentPreparedEvent的SpringApplication的Environment
     * @param application 发布ApplicationEnvironmentPreparedEvent的SpringApplication
     * @param configName bootstrap的配置文件名
     * @return 构建好的Bootstrap容器的ApplicationContext
     */
    private fun bootstrapServiceContext(
        environment: ConfigurableEnvironment, application: SpringApplication, configName: String
    ): ConfigurableApplicationContext {
        // 构建BootstrapEnvironment，并移除掉默认的SystemEnvironment和SystemProperties
        val bootstrapEnvironment = StandardEnvironment()
        val bootstrapProperties = bootstrapEnvironment.getPropertySources()
        bootstrapProperties.forEach { bootstrapProperties.remove(it.name) }

        // 用来进行Bootstrap的Map，当做Bootstrap的PropertySource
        val bootstrapMap = HashMap<String, Any>()
        bootstrapMap["spring.config.name"] = configName  // configName
        bootstrapMap["spring.main.web-application-type"] = "none"  // webType

        // 从Root Environment当中去解析cloud的配置文件的路径...并入到Bootstrap的PropertySources当中去
        // 因为Bootstrap的相关的ApplicationListener在解析过程当中，确实有可能需要用到这些属性
        val cloudConfigLocation = environment.resolvePlaceholders("%{spring.cloud.bootstrap.location:}")
        val additionConfigLocation = environment.resolvePlaceholders("%{spring.cloud.bootstrap.additional-location:}")
        if (StringUtils.hasText(cloudConfigLocation)) {
            bootstrapMap["spring.config.location"] = cloudConfigLocation!!
        }
        if (StringUtils.hasText(additionConfigLocation)) {
            bootstrapMap["spring.config.additional-location"] = additionConfigLocation!!
        }

        // 添加BootstrapMap到BootstrapEnvironment的PropertySource当中
        bootstrapProperties.addFirst(MapPropertySource(BOOTSTRAP_PROPERTY_SOURCE_NAME, bootstrapMap))

        // 将Root Environment当中的PropertySource全部添加到Bootstrap的环境当中，因为有可能Bootstrap会用到...
        environment.getPropertySources().forEach(bootstrapProperties::addLast)

        // 构建Bootstrap的SpringApplicationBuilder...并且替换掉默认的BootstrapEnvironment，因为如果是使用别的类型(比如reactive)将会失败....
        val builder = SpringApplicationBuilder().bannerMode(Banner.Mode.NO)
            .web(ApplicationType.NONE).logStartupInfo(false).environment(bootstrapEnvironment)

        // 如果Builder推断不出来MainApplicationClass，那么需要将Root Application的mainApplicationClass设置进去...
        if (builder.getMainApplicationClass() == null) {
            builder.main(application.getMainApplicationClass())
        }

        // 设置配置类为BootstrapImportSelectorConfiguration，给Bootstrap容器当中注册组件
        builder.sources(BootstrapImportSelectorConfiguration::class.java)

        // 使用builder去进行run，得到BootstrapApplicationContext
        val applicationContext = builder.run()

        // remove BootstrapPropertySource，已经用不上了，直接remove掉...
        bootstrapProperties.remove(BOOTSTRAP_PROPERTY_SOURCE_NAME)

        // 将Bootstrap当中有，但是Environment当中没有，也就是Bootstrap当中新增的部分全部都组合起来并且合并到Environment当中
        mergeDefaultProperties(environment.getPropertySources(), bootstrapProperties)
        // 构建好的BootstrapApplicationContext
        return applicationContext
    }

    /**
     * 将Bootstrap当中**有**，但是Environment当中**没有**，也就是Bootstrap当中新增的部分全部都组合起来并且合并到Environment当中
     *
     * @param environment Root Environment PropertySources
     * @param bootstrap BootStrap Environment PropertySources
     */
    private fun mergeDefaultProperties(environment: MutablePropertySources, bootstrap: MutablePropertySources) {
        val result = CompositePropertySource(DEFAULT_PROPERTIES)

        // 将所有的Bootstrap当中有的，但是Environment当中没有的PropertySource，全部加入到result当中去进行合并到一起...
        bootstrap.forEach {
            if (!environment.contains(it.name)) {
                result.addPropertySource(it)
            }
        }
        // 把result当中的全部，从Bootstrap当中移除掉，因为已经保存下来了...
        result.getPropertySources().forEach { bootstrap.remove(it.name) }

        // 将合并之后的部分(result)添加到environment和bootstrap当中...
        addOrReplace(environment, result)
        addOrReplace(bootstrap, result)
    }

    /**
     * 将result添加到environment当中，或者将result(按照name)替换已经有的PropertySource
     *
     * @param environment EnvironmentPropertySources
     * @param result 想要去添加/替换的PropertySource
     */
    private fun addOrReplace(environment: MutablePropertySources, result: PropertySource<*>) {
        if (environment.contains(result.name)) {
            environment.replace(result.name, result)
        } else {
            environment.addLast(result)
        }
    }

    class BootstrapMarkerConfiguration


    /**
     * 获取排完序的Beans列表
     *
     * @param context beanFactory
     * @param type 要获取的Bean的类型
     */
    private fun <T> getOrderedBeansForType(context: ListableBeanFactory, type: Class<T>): Collection<T> {
        val list = context.getBeansForType(type).values.toMutableList()
        list.sortWith(AnnotationAwareOrderComparator.INSTANCE)
        return list
    }
}