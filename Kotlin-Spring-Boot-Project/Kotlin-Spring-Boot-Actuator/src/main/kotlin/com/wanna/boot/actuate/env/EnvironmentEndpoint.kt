package com.wanna.boot.actuate.env

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.ReadOperation
import com.wanna.framework.core.environment.*
import com.wanna.framework.lang.Nullable
import java.util.function.Predicate
import java.util.regex.Pattern

/**
 * 暴露Spring的Environment的描述信息作为一个Endpoint，供外界调用和查看
 *
 * @param environment Spring的Environment对象，维护各种配置信息
 */
@Endpoint("env")
open class EnvironmentEndpoint(private val environment: ConfigurableEnvironment) {

    /**
     * 暴露环境信息给外界去进行查看，将环境当中的各个属性源的信息都去进行暴露
     *
     * @param pattern 要去进行匹配的正则表达式
     * @return EnvironmentDescriptor(环境描述符)
     */
    @ReadOperation
    open fun environment(@Nullable pattern: String?): EnvironmentDescriptor {
        // 如果没有指定pattern的话，那么Predicate=true
        pattern ?: return getEnvironmentDescriptor { true }
        // 如果指定了Predicate的话，使用正则表达式作为Predicate
        return getEnvironmentDescriptor(Pattern.compile(pattern).asPredicate())
    }

    /**
     * 获取Environment的描述信息，遍历所有的EnumerablePropertySource，去进行描述
     *
     * @param namePredicate 匹配propertyName的Predicate，只有匹配的情况下，才会去进行return
     * @return EnvironmentDescriptor
     */
    private fun getEnvironmentDescriptor(namePredicate: Predicate<String>): EnvironmentDescriptor {
        val propertySources = environment.getPropertySources()
            .filterIsInstance<EnumerablePropertySource<*>>()
            .map { describePropertySource(it.name, it, environment, namePredicate) }
            .toList()
        return EnvironmentDescriptor(environment.getActiveProfiles().toList(), propertySources)
    }

    /**
     * 描述一个PropertySource的相关信息
     *
     * @param propertySourceName PropertySourceName
     * @param propertySource PropertySource
     * @param propertyResolver PropertyResolver
     * @param namePredicate 要去匹配propertyName的断言
     */
    private fun describePropertySource(
        propertySourceName: String,
        propertySource: EnumerablePropertySource<*>,
        propertyResolver: PropertyResolver,
        namePredicate: Predicate<String>
    ): PropertySourceDescriptor {
        val properties = propertySource.getPropertyNames()
            .filter(namePredicate::test)
            .associateWith { describeValueOf(it, propertySource, propertyResolver) }
        return PropertySourceDescriptor(propertySourceName, properties) // return PropertySourceDescriptor
    }

    /**
     * 描述一个具体的属性值，如果必要的话，需要将属性值去进行占位符解析
     *
     * @param name propertyName
     * @param propertySource PropertySource
     * @param resolver PropertyResolver
     * @return PropertyValueDescriptor
     */
    private fun describeValueOf(
        name: String,
        propertySource: PropertySource<*>,
        resolver: PropertyResolver
    ): PropertyValueDescriptor {
        val resolved = resolver.resolvePlaceholders(propertySource.getProperty(name)!!.toString())
        return PropertyValueDescriptor(resolved)
    }

    /**
     * 环境信息的描述符，对Environment中维护的activeProfiles列表和PropertySource列表去进行详细的描述
     *
     * @param activeProfiles activeProfiles
     * @param propertySources PropertySources
     */
    data class EnvironmentDescriptor(
        val activeProfiles: List<String>,
        val propertySources: List<PropertySourceDescriptor>
    )

    /**
     * 一个PropertySource的描述信息
     *
     * @param name propertySource Name
     * @param properties PropertySource当中的各个属性的描述信息
     */
    data class PropertySourceDescriptor(val name: String, val properties: Map<String, PropertyValueDescriptor>)

    /**
     * 针对一个具体的属性值去进行的具体描述信息
     *
     * @param value 属性值(propertyValue)
     */
    data class PropertyValueDescriptor(val value: Any?)
}