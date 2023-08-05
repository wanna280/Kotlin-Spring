package com.wanna.cloud.client.loadbalancer

import com.wanna.boot.autoconfigure.condition.ConditionalOnBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.web.client.RestTemplateCustomizer
import com.wanna.framework.beans.SmartInitializingSingleton
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.web.client.RestTemplate

/**
 * SpringCloud的LoadBalancer的自动配置类, 主要为SpringWebClient当中提供RestTemplate提供LoadBalance的扩展功能
 *
 * 需要在LoadBalancerClient的情况下才去进行自动装配, 因为导入这个依赖的Application, 其实并不一定需要用到负载均衡,
 * 但是如果不进行装配的话, 会出现Autowired注入时, 缺少LoadBalancerClient的情况
 *
 * @see LoadBalancerClient
 */
@ConditionalOnClass(value = [RestTemplate::class])
@ConditionalOnBean(value = [LoadBalancerClient::class])
@Configuration(proxyBeanMethods = false)
open class LoadBalancerAutoConfiguration {

    /**
     * 注入所有要去进行LoadBalance的RestTemplate
     */
    @Autowired
    @LoadBalanced
    private val restTemplates: List<RestTemplate> = emptyList()

    /**
     * 给容器中放入一个SmartInitializingSingleton, 负责在所有的SpringBean完成实例化之后,
     * 提供对需要去进行LoadBalance的所有的RestTemplate的Spring Bean的自定义化的支持
     *
     * @param restTemplateCustomizers RestTemplate的自定义器, 可以对RestTemplate添加自定义配置(自动注入, 可以没有, 没有就算了)
     */
    @Bean
    open fun loadBalancedRestTemplateInitializer(@Autowired(required = false) restTemplateCustomizers: List<RestTemplateCustomizer>): SmartInitializingSingleton {
        return SmartInitializingSingleton {
            restTemplates.forEach { restTemplate ->
                restTemplateCustomizers.forEach { customizer ->
                    customizer.customize(restTemplate)
                }
            }
        }
    }


    /**
     * LoadBalancerRequest的Factory, 提供去创建LoadBalancerRequest, 供LoadBalancerRequest去进行执行
     *
     * @param loadBalancerClient LoadBalancerClient
     * @return 支持负载均衡的处理的RequestFactory
     */
    @ConditionalOnBean(value = [LoadBalancerClient::class])
    @Bean
    @ConditionalOnMissingBean
    open fun loadBalancerRequestFactory(loadBalancerClient: LoadBalancerClient): LoadBalancerRequestFactory {
        return LoadBalancerRequestFactory(loadBalancerClient)
    }

    /**
     * 给[RestTemplate]去添加[LoadBalancerInterceptor]的配置类, 方便去拦截[RestTemplate], 对[RestTemplate]去添加拦截器;
     * 从注册中心当中获取实例, 并将request的URI去进行替换, 从而去实现真正的负载均衡的Http请求的发送
     */
    @ConditionalOnBean(value = [LoadBalancerClient::class])
    @Configuration(proxyBeanMethods = false)
    open class LoadBalancerInterceptorConfig {

        /**
         * LoadBalancer的拦截器, 给RestTemplate当中要去进行导入的拦截器, 替换掉目标请求的URI成为ServiceInstance的URI
         *
         * @param loadBalancerClient 带有负载均衡的Client
         * @param requestFactory 负责均衡的请求的创建的Factory
         */
        @Bean
        open fun loadBalancerInterceptor(
            loadBalancerClient: LoadBalancerClient,
            requestFactory: LoadBalancerRequestFactory
        ): LoadBalancerInterceptor {
            return LoadBalancerInterceptor(loadBalancerClient, requestFactory)
        }

        /**
         * [RestTemplate]的自定义化器, 给它添加上拦截器列表, 用于去为RestTemplate提供负载均衡的功能
         *
         * @param loadBalancerInterceptor 为RestTemplate负载均衡的拦截器
         * @return RestTemplate的Customizer
         */
        @Bean
        open fun restTemplateCustomizer(loadBalancerInterceptor: LoadBalancerInterceptor): RestTemplateCustomizer {
            return RestTemplateCustomizer { restTemplate ->
                val interceptors = ArrayList(restTemplate.getInterceptors())
                interceptors.add(loadBalancerInterceptor)
                restTemplate.setInterceptors(interceptors)
            }
        }
    }


}