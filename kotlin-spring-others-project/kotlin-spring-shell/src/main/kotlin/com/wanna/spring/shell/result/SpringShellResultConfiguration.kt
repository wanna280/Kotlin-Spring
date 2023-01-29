package com.wanna.spring.shell.result

import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * Shell的Result配置类
 */
@Configuration(proxyBeanMethods = false)
open class SpringShellResultConfiguration {

    @Bean
    @Qualifier("main")
    open fun resultHandler(): ResultHandler<Any> {
        return TypeHierarchyResultHandler()
    }
}