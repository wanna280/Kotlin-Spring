package com.wanna.cloud.openfeign

import com.wanna.cloud.openfeign.ribbon.RibbonLoadBalancerFeignClient
import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.FactoryBean
import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.util.StringUtils
import feign.Client
import feign.Contract
import feign.Feign
import feign.Target.HardCodedTarget
import feign.codec.Decoder
import feign.codec.Encoder

/**
 * FeignClient的FactoryBean, 会将一个@FeignClient注解当中的全部元素解析成为一个FactoryBean;
 * 方便后期去进行FeignClient的构建, 本身是一个FactoryBean, 可以通过getTarget方法去回调和创建Bean;
 * 它负责将给定的FeignClient接口使用JDK动态代理去生成代理对象, 方便拦截FeignClient方法, 并在运行时能够正确地去执行请求的发送;
 *
 * @see FeignClient
 * @see EnableFeignClients
 * @see FeignClientsRegistrar
 */
@Suppress("UNCHECKED_CAST")
open class FeignClientFactoryBean : FactoryBean<Any>, InitializingBean, ApplicationContextAware, BeanFactoryAware {

    var type: Class<*>? = null  // type

    var contextId: String? = null  // contextId

    var url: String? = null  // url

    var name: String? = null  // serviceName(childContextName)

    var path: String? = null  // path

    var fallback: Class<*>? = null  // fallback

    var fallbackFactory: Class<*>? = null // fallback Factory

    private var beanFactory: BeanFactory? = null

    private var applicationContext: ApplicationContext? = null

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun getObjectType(): Class<Any> {
        return type as Class<Any>
    }

    override fun getObject(): Any {
        return getTarget()
    }

    /**
     * getTarget, 完成FeignClient的代理工作, 使用JDK动态代理去生成FeignClient的动态代理
     */
    open fun <T> getTarget(): T {
        val beanFactory = this.beanFactory
        val applicationContext = this.applicationContext

        val context = beanFactory?.getBean(FeignContext::class.java)
            ?: (applicationContext?.getBean(FeignContext::class.java)
                ?: throw IllegalStateException("BeanFactory和ApplicationContext不能都为空"))
        // 从ChildContext当中去获取FeignBuilder
        val feign = feign(context)
        // 如果url为空的话, 那么通过serviceName去进行负载均衡...
        if (!StringUtils.hasText(url)) {
            var name = name!!
            // 如果name不是以"http:"作为开头, 那么需要拼接上"http://"协议前缀(Feign当中会检查, 如果没有协议名会报错)
            if (!name.startsWith("http:")) {
                name = "http://$name"
            }
            // 拼接上干净的路径(如果路径不是以"/"开头, 那么拼接上"/", 如果路径是以"/"作为结尾, 那么需要切掉"/")
            name += getCleanPath()
            return loadBalance(feign, context, HardCodedTarget(this.type, name, name)) as T
        }

        // 如果url不为空的话, 那么直接通过url去进行调用即可
        val client = getOptional(context, Client::class.java)
        if (client != null) {
            // 1.如果是LoadBalancerFeignClient的话, 应该获取它包装的client(提供ApacheHttpClient/OkHttp)
            val clientToUse =
                if (client is RibbonLoadBalancerFeignClient) {
                    client.delegate
                } else {
                    client
                }
            feign.client(clientToUse)
        }
        // 获取Targeter
        val targeter = get(context, Targeter::class.java)
        return targeter.target(this, feign, context, HardCodedTarget(type, name, url)) as T
    }

    /**
     * 获取干净的路径, 如果路径不是以"/"开头, 那么拼接上"/", 如果路径是以"/"作为结尾, 那么需要切掉"/"
     */
    private fun getCleanPath(): String {
        var path = this.path
        if (StringUtils.hasText(path)) {
            if (!path!!.startsWith("/")) {
                path = "/$path"
            } else if (path.endsWith("/")) {
                path = path.substring(0, path.length - 1)
            }
        }
        return path ?: ""
    }

    /**
     * 获取FeignBuilder, 并完成FeignBuilder的初始化工作;
     * * (1)Decoder-->负责将RequestBody转为JavaBean
     * * (2)Encoder-->负责将JavaBean写出成为RequestBody
     * * (3)Contract-->扩展Feign默认的Contract, 完成SpringMvc的相关注解的解析
     *
     * @param context FeignContext
     * @return 完成初始化之后的Feign.Builder
     */
    private fun feign(context: FeignContext): Feign.Builder {
        return get(context, Feign.Builder::class.java)
            .decoder(get(context, Decoder::class.java))
            .encoder(get(context, Encoder::class.java))
            .contract(get(context, Contract::class.java))
    }

    /**
     * 进行负载均衡(必须得存在有负载均衡的FeignClient才能走负载均衡...)
     *
     * @param feign FeignBuilder
     * @param context FeignContext
     * @param target target
     * @throws IllegalStateException 如果没有负载均衡的FeignClient, 那么抛出不合法状态异常
     */
    protected open fun <T> loadBalance(feign: Feign.Builder, context: FeignContext, target: HardCodedTarget<T>): T {
        val client = getOptional(context, Client::class.java)
        if (client != null) {
            feign.client(client)
            // 获取Targeter
            val targeter = context.getInstance(contextId!!, Targeter::class.java)!!
            return targeter.target(this, feign, context, target)
        }
        throw IllegalStateException("没有找到用来去进行复杂均衡的FeignClient LoadBalancer")
    }

    /**
     * 从childContext当中去获取指定类型的Bean(获取不到抛异常)
     *
     * @param context FeignContext
     * @param type 想要获取的类型
     * @return return 获取到的Bean, 如果获取到了return Bean; 如果没有获取到, 直接抛出异常
     */
    protected open fun <T : Any> get(context: FeignContext, type: Class<T>): T {
        return context.getInstance(contextId!!, type)!!
    }

    /**
     * 从childContext当中去获取指定类型的Bean(可选)
     *
     * @param context FeignContext
     * @param type 想要获取的类型
     * @return return 获取到的Bean, 如果获取到了return Bean; 如果没有获取到, return null
     */
    protected open fun <T : Any> getOptional(context: FeignContext, type: Class<T>): T? {
        return context.getInstance(contextId!!, type)
    }

    override fun isSingleton() = true
    override fun isPrototype() = false

    override fun afterPropertiesSet() {

    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}