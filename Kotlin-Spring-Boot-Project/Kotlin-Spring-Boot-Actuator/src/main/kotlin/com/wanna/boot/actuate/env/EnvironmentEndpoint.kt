package com.wanna.boot.actuate.env

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.ReadOperation
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.EnumerablePropertySource
import com.wanna.framework.core.environment.Environment

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
     * @return EnvironmentDescriptor(环境描述符)
     */
    @ReadOperation
    open fun environment(): EnvironmentDescriptor {
        val propertySources = environment.getPropertySources()
            .filterIsInstance<EnumerablePropertySource<*>>()
            .map { propertySource ->
                val properties = HashMap<String, PropertyValueDescriptor>()
                propertySource.getPropertyNames().forEach { propertyName ->
                    properties[propertyName] = PropertyValueDescriptor(environment.getProperty(propertyName))
                }
                PropertySourceDescriptor(propertySource.name, properties) // return PropertySourceDescriptor
            }.toList()
        return EnvironmentDescriptor(environment.getActiveProfiles().toList(), propertySources)
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