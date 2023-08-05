package com.wanna.framework.core.environment

/**
 * SystemEnvironment的[PropertySource], 特殊的标识, 方便根据类型去进行判断是否是SystemEnvironment
 *
 * @param name property source name
 * @param source map
 *
 * @see System.getenv
 */
open class SystemEnvironmentPropertySource(name: String, source: Map<String, Any>) : MapPropertySource(name, source)