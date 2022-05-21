package com.wanna.cloud.netflix.ribbon

import com.netflix.client.config.CommonClientConfigKey
import com.netflix.client.config.DefaultClientConfigImpl
import com.netflix.client.config.IClientConfig
import com.netflix.client.config.IClientConfigKey
import com.netflix.loadbalancer.ConfigurationBasedServerList
import com.netflix.loadbalancer.DummyPing
import com.netflix.loadbalancer.ILoadBalancer
import com.netflix.loadbalancer.IPing
import com.netflix.loadbalancer.IRule
import com.netflix.loadbalancer.PollingServerListUpdater
import com.netflix.loadbalancer.Server
import com.netflix.loadbalancer.ServerList
import com.netflix.loadbalancer.ServerListFilter
import com.netflix.loadbalancer.ServerListUpdater
import com.netflix.loadbalancer.ZoneAffinityServerListFilter
import com.netflix.loadbalancer.ZoneAvoidanceRule
import com.netflix.loadbalancer.ZoneAwareLoadBalancer
import com.wanna.boot.autoconfigure.condition.ConditionOnMissingBean
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.beans.factory.annotation.Value
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.core.environment.Environment

@EnableConfigurationProperties
@Configuration(proxyBeanMethods = false)
open class RibbonClientConfiguration : EnvironmentAware {

    private var environment: Environment? = null

    @Value("%{ribbon.client.name}")
    private var clientName: String = "client"

    /**
     * 给childContext当中去导入Client的配置信息
     */
    @Bean
    @ConditionOnMissingBean
    open fun ribbonClientConfig(): IClientConfig {
        val config = DefaultClientConfigImpl()
        config.loadProperties(clientName)
        config.set(
            CommonClientConfigKey.ConnectTimeout, getProperty(CommonClientConfigKey.ConnectTimeout, 1000)
        )
        config.set(
            CommonClientConfigKey.ReadTimeout, getProperty(CommonClientConfigKey.ReadTimeout, 1000)
        )
        config.set(CommonClientConfigKey.GZipPayload, true)
        return config
    }

    /**
     * 给容器中导入一个默认的负载均衡的规则，配置ILoadBalancer去完成
     */
    @Bean
    @ConditionOnMissingBean
    open fun ribbonRule(config: IClientConfig): IRule {
        val rule = ZoneAvoidanceRule()
        rule.initWithNiwsConfig(config)
        return rule
    }

    @Bean
    @ConditionOnMissingBean
    open fun ribbonPing(): IPing {
        return DummyPing()
    }

    @Bean
    @ConditionOnMissingBean
    open fun ribbonServerList(config: IClientConfig): ServerList<Server> {
        val serverList = ConfigurationBasedServerList()
        serverList.initWithNiwsConfig(config)
        return serverList
    }

    @Bean
    @ConditionOnMissingBean
    open fun ribbonServerListUpdater(config: IClientConfig): ServerListUpdater {
        return PollingServerListUpdater(config)
    }

    @Bean
    @ConditionOnMissingBean
    open fun ribbonServerListFilter(config: IClientConfig): ServerListFilter<Server> {
        val filter = ZoneAffinityServerListFilter<Server>()
        filter.initWithNiwsConfig(config)
        return filter
    }

    @Bean
    @ConditionOnMissingBean
    open fun ribbonLoadBalancer(
        config: IClientConfig,
        serverList: ServerList<Server>,
        serverListFilter: ServerListFilter<Server>,
        rule: IRule,
        ping: IPing,
        serverListUpdater: ServerListUpdater
    ): ILoadBalancer {
        return ZoneAwareLoadBalancer(config, rule, ping, serverList, serverListFilter, serverListUpdater)
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    private fun getProperty(
        connectTimeout: IClientConfigKey<Int>, defaultConnectTimeout: Int
    ): Int? {
        return environment!!.getProperty(
            "ribbon.$connectTimeout", Int::class.java, defaultConnectTimeout
        )
    }
}