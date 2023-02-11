package com.wanna.cloud.netflix.ribbon

import com.netflix.client.config.CommonClientConfigKey.*
import com.netflix.client.config.DefaultClientConfigImpl
import com.netflix.client.config.IClientConfig
import com.netflix.client.config.IClientConfigKey
import com.netflix.loadbalancer.*
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.beans.factory.annotation.Value
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.core.environment.Environment

/**
 * RibbonClient的配置类, RibbonClient要想实现负载均衡, 必须组合ILoadBalancer去完成负载均衡, 需要使用到ILoadBalancer,
 * 则需要提供IClientConfig、IRule、IPing、ServerList、ServerFilterList、ServerListUpdater这六个组件, 在这个配置类当中, 我们都提供默认的;
 * 在真正地提供负载均衡的Server(ServiceInstance)一方(比如Nacos、Eureka等), 只需要按需替换掉配置的默认配置即可实现让Ribbon去完成负载均衡;
 * 比如Nacos当中, 就替换掉默认的ServerList去让Ribbon知道, Nacos的DiscoveryServer当中根据serviceId能够去, 找到哪些实例列表;
 *
 * @see IClientConfig
 * @see IRule
 * @see IPing
 * @see ServerListUpdater
 * @see ServerList
 * @see ServerListFilter
 * @see ILoadBalancer
 */
@EnableConfigurationProperties
@Configuration(proxyBeanMethods = false)
open class RibbonClientConfiguration : EnvironmentAware {

    private var environment: Environment? = null

    @Value("${'$'}{ribbon.client.name}")
    private var clientName: String = "client"

    /**
     * 给childContext当中去导入Client的配置信息
     */
    @Bean
    @ConditionalOnMissingBean
    open fun ribbonClientConfig(): IClientConfig {
        val config = DefaultClientConfigImpl()
        config.loadProperties(clientName)
        config.set(ConnectTimeout, getProperty(ConnectTimeout, 1000))
        config.set(ReadTimeout, getProperty(ReadTimeout, 1000))
        config.set(GZipPayload, true)
        return config
    }

    /**
     * 给容器中导入一个默认的负载均衡的规则, 配置ILoadBalancer去完成
     */
    @Bean
    @ConditionalOnMissingBean
    open fun ribbonRule(config: IClientConfig): IRule {
        val rule = ZoneAvoidanceRule()
        rule.initWithNiwsConfig(config)
        return rule
    }

    @Bean
    @ConditionalOnMissingBean
    open fun ribbonPing(): IPing {
        return DummyPing()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun ribbonServerList(config: IClientConfig): ServerList<Server> {
        val serverList = ConfigurationBasedServerList()
        serverList.initWithNiwsConfig(config)
        return serverList
    }

    @Bean
    @ConditionalOnMissingBean
    open fun ribbonServerListUpdater(config: IClientConfig): ServerListUpdater {
        return PollingServerListUpdater(config)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun ribbonServerListFilter(config: IClientConfig): ServerListFilter<Server> {
        val filter = ZoneAffinityServerListFilter<Server>()
        filter.initWithNiwsConfig(config)
        return filter
    }

    /**
     * ILoadBalancer, 去提供负载均衡的Server的选择
     */
    @Bean
    @ConditionalOnMissingBean
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

    private fun getProperty(connectTimeout: IClientConfigKey<Int>, defaultConnectTimeout: Int): Int? {
        return environment!!.getProperty("ribbon.$connectTimeout", Int::class.java, defaultConnectTimeout)
    }
}