package com.wanna.spring.shell.command

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
open class ShellCommandAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun quit(): Quit {
        return Quit()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun help(): Help {
        return Help()
    }
}