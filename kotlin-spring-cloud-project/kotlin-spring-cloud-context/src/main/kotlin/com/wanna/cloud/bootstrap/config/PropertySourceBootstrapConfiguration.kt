package com.wanna.cloud.bootstrap.config

import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.environment.EnumerablePropertySource
import com.wanna.framework.core.environment.PropertySource

/**
 * 这是一个PropertySource的Bootstrap配置类, 能够自动注入Bootstrap当中的所有的PropertySourceLocator, 并进行保存;
 *
 * ## Note
 * 因为它是一个Bootstrap当中的ApplicationContextInitializer, 因此它会被转移到Root Application当中去进行apply...
 * 它并不会在Bootstrap当中生效, 因为Bootstrap当中ApplicationContextInitializer生效时, 这个组件都还没被扫描到容器当中呢！
 */
@Configuration(proxyBeanMethods = false)
open class PropertySourceBootstrapConfiguration : ApplicationContextInitializer<ConfigurableApplicationContext>,
    Ordered {

    private var order: Int = Ordered.ORDER_HIGHEST + 10

    /**
     * 自动注入所有的PropertySourceLocator
     */
    @Autowired(required = false)
    private var propertySourceLocators: List<PropertySourceLocator> = ArrayList()

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val composite = ArrayList<PropertySource<*>>()
        val environment = applicationContext.getEnvironment()
        this.propertySourceLocators.forEach {
            val propertySources = it.locateCollection(environment)
            propertySources.forEach {
                if (it is EnumerablePropertySource<*>) {
                    composite += BootstrapPropertySource(it)
                } else {
                    composite += SimpleBootstrapPropertySource(it)
                }
            }
        }

        // addFirst
        composite.forEach { environment.getPropertySources().addFirst(it) }
    }

    override fun getOrder(): Int {
        return this.order
    }

    open fun setOrder(order: Int) {
        this.order = order
    }

}