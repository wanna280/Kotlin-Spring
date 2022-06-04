package com.wanna.cloud.client.loadbalancer

import com.wanna.boot.autoconfigure.condition.ConditionOnMissingBean
import com.wanna.framework.beans.SmartInitializingSingleton
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.web.client.RestTemplate

/**
 * SpringCloud的LoadBalancer的自动配置类，主要为SpringWebClient当中提供RestTemplate提供LoadBalance的扩展功能
 */
@Configuration(proxyBeanMethods = false)
open class LoadBalancerAutoConfiguration {

    @Autowired
    @LoadBalanced  // 注入所有要去进行LoadBalance的RestTemplate
    private val restTemplates: List<RestTemplate> = emptyList()

    /**
     * 给容器中放入一个SmartInitializingSingleton，负责在所有的SpringBean完成实例化之后，
     * 提供对需要去进行LoadBalance的所有的RestTemplate的Spring Bean的自定义化的支持
     *
     * @param restTemplateCustomizers RestTemplate的自定义器，可以对RestTemplate添加自定义配置(自动注入，可以没有，没有就算了)
     */
    @Bean
    open fun loadBalancedRestTemplateInitializer(@Autowired(required = false) restTemplateCustomizers: List<RestTemplateCustomizer>): SmartInitializingSingleton {
        return object : SmartInitializingSingleton {
            override fun afterSingletonsInstantiated() {
                restTemplates.forEach { restTemplate ->
                    restTemplateCustomizers.forEach { customizer ->
                        customizer.customize(restTemplate)
                    }
                }
            }
        }
    }

    /**
     * 给RestTemplate去添加LoadBalancerInterceptor的配置类，方便去拦截RestTemplate，对RestTemplate去添加拦截器；
     * 从注册中心当中获取实例，并将request的URI去进行替换，从而去实现真正的负载均衡的Http请求的发送
     */
    @Configuration(proxyBeanMethods = false)
    open class LoadBalancerInterceptorConfig {

        /**
         * LoadBalancerRequest的Factory，提供去创建LoadBalancerRequest，供LoadBalancerRequest去进行执行
         */
        @Bean
        @ConditionOnMissingBean
        open fun loadBalancerRequestFactory(loadBalancerClient: LoadBalancerClient): LoadBalancerRequestFactory {
            return LoadBalancerRequestFactory(loadBalancerClient)
        }

        /**
         * LoadBalancer的拦截器，给RestTemplate当中要去进行导入的拦截器，替换掉目标请求的URI成为ServiceInstance的URI
         */
        @Bean
        open fun loadBalancerInterceptor(
            loadBalancerClient: LoadBalancerClient,
            requestFactory: LoadBalancerRequestFactory
        ): LoadBalancerInterceptor {
            return LoadBalancerInterceptor(loadBalancerClient, requestFactory)
        }

        @Bean
        open fun restTemplateCustomizer(loadBalancerInterceptor: LoadBalancerInterceptor): RestTemplateCustomizer {
            return object : RestTemplateCustomizer {
                override fun customize(restTemplate: RestTemplate) {
                    val interceptors = ArrayList(restTemplate.getInterceptors())
                    interceptors.add(loadBalancerInterceptor)
                    restTemplate.setInterceptors(interceptors)
                }
            }
        }
    }


}