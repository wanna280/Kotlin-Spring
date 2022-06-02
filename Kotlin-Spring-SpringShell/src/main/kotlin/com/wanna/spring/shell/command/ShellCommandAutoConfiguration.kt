package com.wanna.spring.shell.command

import com.wanna.boot.autoconfigure.condition.ConditionOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
open class ShellCommandAutoConfiguration {

    @Bean
    @ConditionOnMissingBean
    open fun quit(): Quit {
        return Quit()
    }

    @Bean
    @ConditionOnMissingBean
    open fun help(): Help {
        return Help()
    }
}