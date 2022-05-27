package com.wanna.boot.autoconfigure.condition

import com.wanna.boot.autoconfigure.AutoConfigurationImportFilter
import com.wanna.boot.autoconfigure.AutoConfigurationMetadata
import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.core.util.ClassUtils

/**
 * 这是一个同时实现AutoConfigurationImportFilter和SpringBootCondition的一个SpringBootCondition；
 * 它新增了使用AutoConfigurationImportFilter的方式去对自动配置的配置类去进行过滤的方式，它的实现方式包括OnBeanCondition/OnClassCondition等；
 * 它会在AutoConfigurationImportSelector当中被回调到，去使用AutoConfigurationImportFilter的matches方式去进行回调
 *
 * @see OnBeanCondition
 * @see OnClassCondition
 * @see com.wanna.boot.autoconfigure.AutoConfigurationImportSelector
 */
@Suppress("UNCHECKED_CAST")
abstract class FilteringSpringBootCondition : SpringBootCondition(), AutoConfigurationImportFilter,
    BeanClassLoaderAware, BeanFactoryAware {

    private lateinit var classLoader: ClassLoader

    private lateinit var beanFactory: BeanFactory

    /**
     * 这是一个来自于AutoConfigurationImportFilter的匹配方法，主要是基于配置文件当中的元信息去进行匹配
     *
     * @see AutoConfigurationImportFilter.matches
     * @see com.wanna.boot.autoconfigure.AutoConfigurationImportSelector.ConfigurationClassFilter.filter
     * @param autoConfigurationClasses SpringFactories当中要导入的自动配置类的候选className列表(某个元素可能为null，代表之前的Filter已经将其过滤掉了)
     * @param autoConfigurationMetadata Metadata配置文件当中要进行匹配的元信息
     */
    override fun matches(
        autoConfigurationClasses: Array<String?>, autoConfigurationMetadata: AutoConfigurationMetadata
    ): Array<Boolean> {
        // 获取子类当中对于自动配置类的匹配结果(Array<ConditionOutcome?>)，其中的某个元素有可能为null
        val outcomes = getOutcomes(autoConfigurationClasses, autoConfigurationMetadata)
        val matches = arrayOfNulls<Boolean>(outcomes.size)
        for (index in outcomes.indices) {
            /// 对于index位置的匹配结果是，如果return null/isMatch的话，才为true
            matches[index] = outcomes[index] == null || outcomes[index]!!.match
            // 如果匹配失败，并且outcome不为空的话，需要记录日志信息
            if (matches[index] == false && outcomes[index] != null) {
                logOutcome(autoConfigurationClasses[index], outcomes[index]!!)
            }
        }
        return matches as Array<Boolean>  // cast to not null array
    }

    /**
     * 这是一个模板方法，交给子类去实现，对autoConfigurationClasses当中去进行配置的类去进行匹配(某个元素可能为null，代表之前的Filter已经将其过滤掉了)；
     * 返回的Array<ConditionOutcome?>和autoConfigurationClasses数组等长，index对应的结果为null或者outcome.isMatch的话，该自动配置类应该导入；
     * 如果outcome.isNotMatch，那么说明该配置类不应该被装配到容器当中；实际上SpringBoot当中的大多数的自动配置类，也都会在这里就被过滤掉，不会成为候选
     */
    abstract fun getOutcomes(
        autoConfigurationClasses: Array<String?>, autoConfigurationMetadata: AutoConfigurationMetadata
    ): Array<ConditionOutcome?>

    /**
     * 将指定的classNames，经过filter的过滤之后，剩下的符合条件的className将会被返回
     *
     * @param classNames 原始的classNames(可以为null)
     * @param filter 如何去过滤？可以使用PRESENT/MISSING两种方式去过滤
     * @return 如果原始classNames不为空，那么返回经过filter过滤之后的classNames；如果原始classNames为空，那么return 空的List
     */
    protected fun filter(classNames: Collection<String>?, filter: ClassNameFilter, classLoader: ClassLoader) =
        if (classNames == null || classNames.isEmpty()) emptyList()
        else classNames.filter { filter.matches(it, classLoader) }.toMutableList()

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.classLoader = classLoader
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    open fun getClassLoader(): ClassLoader = this.classLoader
    open fun getBeanFactory(): BeanFactory = this.beanFactory

    /**
     * 这是一个ClassName的Filter，判断className所在类是否存在于依赖当中
     */
    protected enum class ClassNameFilter {
        PRESENT {
            override fun matches(className: String, classLoader: ClassLoader) = isPresent(className, classLoader)
        },
        MISSING {
            override fun matches(className: String, classLoader: ClassLoader) = !isPresent(className, classLoader)
        };

        // 匹配方法，抽象方法，内部的枚举单例对象也都必须去实现这个方法
        abstract fun matches(className: String, classLoader: ClassLoader): Boolean

        // **枚举类内部还能定义抽象方法！！！**
        companion object {
            /**
             * 根据className去判断该类是否已经存在于依赖当中
             * @param className className to matches
             * @param classLoader classLoader for Class.forName
             */
            @JvmStatic
            fun isPresent(className: String, classLoader: ClassLoader): Boolean {
                return ClassUtils.isPresent(className, classLoader)
            }
        }
    }
}