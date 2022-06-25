package com.wanna.cloud.autoconfigure

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.cloud.context.endpoint.event.RefreshEventListener
import com.wanna.cloud.context.refresh.ContextRefresher
import com.wanna.cloud.context.scope.refresh.RefreshScope
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * 自动完成Refresh的自动配置类，自动导入Refresh相关的组件
 */
@Configuration
open class RefreshAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean([RefreshScope::class])
    open fun refreshScope(): RefreshScope {
        return RefreshScope()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun contextRefresher(context: ConfigurableApplicationContext, scope: RefreshScope): ContextRefresher {
        return ContextRefresher(context, scope)
    }

    @Bean
    open fun refreshEventListener(contextRefresher: ContextRefresher): RefreshEventListener {
        return RefreshEventListener(contextRefresher)
    }
}