package com.wanna.boot.actuate.beans

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.ReadOperation
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * Spring的Beans的Endpoint，将Spring的ApplicationContext当中的所有的Bean暴露给用户
 *
 * @param context 要去继续暴露的ApplicationContext
 */
@Endpoint("beans")
open class BeansEndpoint(private val context: ConfigurableApplicationContext) {

    /**
     * 暴露一个读操作给外界去进行获取ApplicationContext当中的所有的Bean的信息，
     * 会遍历ApplicationContext以及所有的parentApplicationContext去进行检查
     *
     * @return 暴露给外界的ApplicationBeans
     */
    @ReadOperation
    open fun beans(): ApplicationBeans {
        val contexts = HashMap<String, ContextBeans>()
        var context: ConfigurableApplicationContext? = this.context
        while (context != null) {
            contexts[context.getId() ?: ""] = ContextBeans.describing(context)
            val parent = context.getParent()
            context = if (parent is ConfigurableApplicationContext) parent else null
        }
        return ApplicationBeans(contexts)
    }

    /**
     * 其中维护着指定的ApplicationContext，以及它的parentApplicationContext当中的所有的Bean的列表的信息
     *
     * @param contexts key-Spring当中的ApplicationContext的id, value-该ApplicationContext当中的所有的Bean的列表
     */
    data class ApplicationBeans(val contexts: Map<String, ContextBeans>)

    /**
     * 维护一个ApplicationContext当中的Bean的列表(key-beanName, value=BeanDescriptor)
     *
     * @param beans Map of "beanName->BeanDescriptor"
     */
    data class ContextBeans(val beans: Map<String, BeanDescriptor>) {
        companion object {
            /**
             * 描述一个ApplicationContext当中的所有的Bean的信息
             *
             * @param context 要去进行描述的ApplicationContext
             * @return 描述完成的ContextBeans
             */
            @JvmStatic
            fun describing(context: ConfigurableApplicationContext): ContextBeans {
                return describeBeans(context.getBeanFactory())
            }

            /**
             * 描述一个BeanFactory当中的所有的Bean
             *
             * @param beanFactory 要去进行描述的BeanFactory
             * @return 构建好的ContextBeans
             */
            @JvmStatic
            private fun describeBeans(beanFactory: ConfigurableListableBeanFactory): ContextBeans {
                val beans = HashMap<String, BeanDescriptor>()
                val beanDefinitionNames = beanFactory.getBeanDefinitionNames()
                beanDefinitionNames.forEach { beanName ->
                    if (isBeanEligible(beanName, beanFactory.getBeanDefinition(beanName), beanFactory)) {
                        beans[beanName] = describeBean(beanName, beanFactory.getBeanDefinition(beanName), beanFactory)
                    }
                }
                return ContextBeans(beans)
            }

            /**
             * 描述一个Bean，需要去构建一个BeanDescriptor
             *
             * @param beanFactory beanFactory
             * @param beanName beanName
             * @param definition BeanDeinition
             */
            @JvmStatic
            private fun describeBean(
                beanName: String,
                definition: BeanDefinition,
                beanFactory: ConfigurableListableBeanFactory
            ): BeanDescriptor {
                return BeanDescriptor(definition.getScope(), beanFactory.getType(beanName)!!)
            }

            /**
             * 判断一个Bean是否是有资格去进行暴露的？(Note: 对于一个基础设施Bean，我们不去进行暴露给用户)
             *
             * @param beanFactory beanFactory
             * @param beanName beanName
             * @param definition BeanDefinition
             */
            @JvmStatic
            private fun isBeanEligible(
                beanName: String,
                definition: BeanDefinition,
                beanFactory: ConfigurableListableBeanFactory
            ): Boolean {
                return definition.getRole() != BeanDefinition.ROLE_INFRASTRUCTURE
                        && (!definition.isLazyInit() || beanFactory.containsSingleton(beanName))
            }
        }
    }

    /**
     * 对SpringBeanFactory当中的一个Bean的描述符
     *
     * @param scope scopeName
     * @param type beanType
     */
    data class BeanDescriptor(val scope: String, val type: Class<*>)
}