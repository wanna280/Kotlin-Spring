package com.wanna.framework.test.context

import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * 对于[ContextConfiguration]注解封装得到的属性信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
class ContextConfigurationAttributes(val declaringClass: Class<*>) {

    var locations: Array<String> = emptyArray()

    var classes: Array<Class<*>> = emptyArray()

    var initializers: Array<Class<out ApplicationContextInitializer<out ConfigurableApplicationContext>>> = emptyArray()

    var contextLoaderClass: Class<out ContextLoader> = ContextLoader::class.java

    constructor(declaringClass: Class<*>, contextConfiguration: ContextConfiguration) : this(declaringClass) {
        this.locations = contextConfiguration.locations + contextConfiguration.value
        this.contextLoaderClass = contextConfiguration.loader.java
        this.classes = contextConfiguration.classes.map { it.java }.toTypedArray()
        this.initializers = contextConfiguration.initializers.map { it.java }.toTypedArray()
    }
}