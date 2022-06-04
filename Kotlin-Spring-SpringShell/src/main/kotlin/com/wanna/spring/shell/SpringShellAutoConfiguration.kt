package com.wanna.spring.shell

import com.wanna.boot.ApplicationRunner
import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Import
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.spring.shell.annotation.ShellCommandMethodTargetRegistrar
import com.wanna.spring.shell.result.ResultHandler
import com.wanna.spring.shell.result.SpringShellResultConfiguration

/**
 * SpringShell的自动配置类
 */
@Configuration
@Import([ShellCommandMethodTargetRegistrar::class, SpringShellResultConfiguration::class])
open class SpringShellAutoConfiguration {

    @Bean
    @Qualifier("springShellConversionService")
    open fun conversionService(): ConversionService {
        return DefaultConversionService()
    }

    @Bean
    @Qualifier("commandLineApplicationRunner")
    open fun commandLineApplicationRunner(): ApplicationRunner {
        return InteractiveShellApplicationRunner()
    }

    @Bean
    @Qualifier("springShell")
    open fun shell(@Qualifier("main") resultHandler: ResultHandler<Any>): Shell {
        return Shell(resultHandler)
    }
}