package com.wanna.cloud.autoconfigure

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.cloud.endpoint.event.RefreshEventListener
import com.wanna.cloud.context.refresh.ContextRefresher
import com.wanna.cloud.context.scope.refresh.RefreshScope
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * 自动完成Refresh的自动配置类, 自动导入Refresh相关的组件
 */
@Configuration(proxyBeanMethods = false)
open class RefreshAutoConfiguration {

    /**
     * 给容器当中导入一个RefreshScope, 用来维护RefreshScope内的Bean的列表
     */

    @Bean
    @ConditionalOnMissingBean([RefreshScope::class])
    open fun refreshScope(): RefreshScope {
        return RefreshScope()
    }

    /**
     * SpringContext的Refresher, 提供Environment以及RefreshScope内的Bean的刷新工作
     */
    @Bean
    @ConditionalOnMissingBean
    open fun contextRefresher(context: ConfigurableApplicationContext, scope: RefreshScope): ContextRefresher {
        return ContextRefresher(context, scope)
    }

    /**
     * 负责监听RefreshEvent, 当该事件到来时, 会知道哪个使用ContextRefresher去进行refresh, 从而实现动态刷新;
     * 对于动态的配置中心的实现(例如Nacos), 只需要发布该事件, 即可实现@ConfigurationProperties的Bean以及RefrechScope内的Bean去进行刷新
     */
    @Bean
    open fun refreshEventListener(contextRefresher: ContextRefresher): RefreshEventListener {
        return RefreshEventListener(contextRefresher)
    }
}