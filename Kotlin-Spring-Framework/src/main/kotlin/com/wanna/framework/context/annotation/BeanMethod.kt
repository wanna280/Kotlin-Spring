package com.wanna.framework.context.annotation

import com.wanna.framework.core.type.MethodMetadata

/**
 * 标识这是一个BeanMethod，也就是被@Bean标注的方法
 */
open class BeanMethod(_metadata: MethodMetadata, _configClass: ConfigurationClass) :
    ConfigurationMethod(_metadata, _configClass) {

    override fun toString(): String {
        return this.metadata.getDeclaringClassName() + "." + this.metadata.getMethodName()
    }
}