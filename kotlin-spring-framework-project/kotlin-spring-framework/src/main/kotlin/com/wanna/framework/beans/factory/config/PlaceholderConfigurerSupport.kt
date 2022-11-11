package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.context.aware.BeanNameAware
import java.util.*

/**
 * 提供对于占位符的解析的基础支持类，它的父类当中已经实现了对于[Properties]的加载和后置处理，
 * 在这个类当中，我们可以利用这些[Properties]去提供占位符的解析功能，提供了一些解析占位符的模板方法功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/22
 */
abstract class PlaceholderConfigurerSupport : PropertyResourceConfigurer(), BeanNameAware, BeanFactoryAware {

    companion object {
        /**
         * 默认的占位符的前缀
         */
        const val DEFAULT_PLACEHOLDER_PREFIX = "%{"

        /**
         * 默认的占位符的后缀
         */
        const val DEFAULT_PLACEHOLDER_SUFFIX = "}"

        /**
         * 默认值的分隔符
         */
        const val DEFAULT_VALUE_SEPARATOR = ":"
    }

    /**
     * 占位符前缀
     */
    var placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX

    /**
     * 占位符后缀
     */
    var placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX

    /**
     * 默认值的分隔符
     */
    var valueSeparator = DEFAULT_VALUE_SEPARATOR


    /**
     * 是否需要trim掉值？
     */
    var trimValues: Boolean = false


    /**
     * 是否需要忽略无法解析的占位符？
     */
    var ignoreUnresolvablePlaceholders = false

    /**
     * 当遇到这样一个值的时候，需要被当做null去进行处理，比如"null"/""
     */
    var nullValue: String? = null

    /**
     * BeanFactory
     */
    private var beanFactory: BeanFactory? = null

    /**
     * 当前Bean的beanName
     */
    private var beanName: String? = null

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun setBeanName(beanName: String) {
        this.beanName = beanName
    }

    /**
     * 对于属性的处理工作，这里会将给定的[StringValueResolver]去添加到BeanFactory当中，
     * 这样BeanFactory就新增了对于占位符的解析功能，作为一个模板方法交给子类去进行使用
     *
     * @param beanFactory BeanFactory
     * @param valueResolver StringValueResolver，提供对于占位符的解析功能
     */
    protected open fun doProcessProperties(
        beanFactory: ConfigurableListableBeanFactory,
        valueResolver: StringValueResolver
    ) {
        val beanDefinitionVisitor = BeanDefinitionVisitor(valueResolver)

        // 遍历所有的BeanDefinition，去进行占位符的解析功能
        val beanDefinitionNames = beanFactory.getBeanDefinitionNames()
        beanDefinitionNames.forEach {
            if (it != beanName && this.beanFactory == beanFactory) {
                beanDefinitionVisitor.visitBeanDefinition(beanFactory.getBeanDefinition(it))
            }
        }

        // 将StringValueResolver加入到BeanFactory当中，提供对于BeanFactory的一些占位符的解析功能
        beanFactory.addEmbeddedValueResolver(valueResolver)
    }
}