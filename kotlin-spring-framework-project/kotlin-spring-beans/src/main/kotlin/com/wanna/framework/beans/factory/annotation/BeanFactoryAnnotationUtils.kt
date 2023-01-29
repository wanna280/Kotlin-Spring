package com.wanna.framework.beans.factory.annotation

import com.wanna.framework.beans.BeansException
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.BeanFactoryUtils
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.beans.factory.config.ConfigurableBeanFactory
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.factory.exception.NoSuchBeanDefinitionException
import com.wanna.framework.beans.factory.exception.NoUniqueBeanDefinitionException
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.lang.Nullable
import java.util.function.Predicate
import kotlin.jvm.Throws

/**
 * BeanFactory当中的和Qualifier相关操作的工具类, 提供针对BeanFactory当中的Qualifier的Bean的探测的相关操作
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 *
 * @see BeanFactoryUtils
 * @see Qualifier
 */
object BeanFactoryAnnotationUtils {

    /**
     * 从BeanFactory当中, 去获取到所有的Qualifier和beanType都匹配的Bean
     *
     * @param beanFactory ListableBeanFactory
     * @param beanType beanType
     * @param qualifier Bean Qualifier
     * @return 统计得到的所有的匹配的Bean的相关信息的Map, Key-beanName, Value-Bean
     */
    @JvmStatic
    fun <T : Any> qualifiedBeansOfType(
        beanFactory: ListableBeanFactory,
        beanType: Class<T>,
        qualifier: String
    ): Map<String, T> {
        val beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, beanType)
        val result = LinkedHashMap<String, T>()
        for (beanName in beanNames) {
            // 如果beanName就匹配的话, 那么直接收集起来;
            // 如果beanName不匹配的话, 那么从BeanFactory当中去获取到对应的Bean的beanType去进行匹配
            if (isQualifierMatch({ it == qualifier }, beanName, beanFactory)) {
                result[beanName] = beanFactory.getBean(beanName, beanType)
            }
        }
        return result
    }

    /**
     * 从BeanFactory当中去获取到beanType和Qualifier都匹配的Bean;
     *
     * * 1.如果找到了一个合适的, return Bean,
     * * 2.如果找到了多个合适的, 那么丢出[NoUniqueBeanDefinitionException]异常,
     * * 3.如果一个合适的都没有找到的话, 那么丢出[NoSuchBeanDefinitionException]异常
     *
     * @param beanFactory BeanFactory
     * @param beanType beanType
     * @param qualifier qualifier
     * @throws NoSuchBeanDefinitionException 如果无法在BeanFactory当中去找到合适的Bean
     * @throws NoUniqueBeanDefinitionException 如果无法在BeanFactory当中去找到唯一的Qualifier的Bean
     */
    @Throws(NoUniqueBeanDefinitionException::class, NoSuchBeanDefinitionException::class)
    @JvmStatic
    fun <T : Any> qualifiedBeanOfType(beanFactory: BeanFactory, beanType: Class<T>, qualifier: String): T {
        if (beanFactory is ListableBeanFactory) {
            return qualifiedBeanOfType(lbf = beanFactory, beanType = beanType, qualifier = qualifier)
        } else if (beanFactory.containsBean(qualifier)) {
            return beanFactory.getBean(qualifier, beanType)
        } else {
            throw NoSuchBeanDefinitionException("没有从BeanFactory当中根据beanName(qualifier)=[$qualifier]去获取到Bean, 并且BeanFactory还不是ListableBeanFactory, 因此无法根据qualifier去对所有的Bean去进行逐一的qualifier匹配")
        }
    }

    /**
     * 从BeanFactory当中去获取到beanType和Qualifier都匹配的Bean
     *
     * @param lbf ListableBeanFactory
     * @param beanType beanType
     * @param qualifier qualifier
     * @throws NoUniqueBeanDefinitionException 如果在BeanFactory当中找到qualifier匹配的Bean不止一个的话
     * @throws NoSuchBeanDefinitionException 如果BeanFactory没有去找到合适的Qualifier的Bean
     */
    @Throws(NoUniqueBeanDefinitionException::class, NoSuchBeanDefinitionException::class)
    @JvmStatic
    private fun <T : Any> qualifiedBeanOfType(lbf: ListableBeanFactory, beanType: Class<T>, qualifier: String): T {
        val candidates = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(lbf, beanType)
        var result: String? = null
        for (candidate in candidates) {
            if (isQualifierMatch({ qualifier == it }, candidate, lbf)) {
                // 如果之前已经找到过了qualifier匹配的Bean的话, 那么需要丢出来NoUniqueBeanDefinitionException
                if (result != null) {
                    throw NoUniqueBeanDefinitionException("在BeanFactory当中, qualifier=[$qualifier]的Bean不唯一, founded=[$candidate,$result]")
                }
                result = candidate
            }
        }
        if (result != null) {
            return lbf.getBean(result, beanType)
        } else if (lbf.containsBean(qualifier)) {
            return lbf.getBean(qualifier, beanType)
        } else {
            throw NoSuchBeanDefinitionException("没有在BeanFactory当中根据qualifier=[$qualifier]去找到合适的Bean, 根据qualifier/beanName去进行寻找都没有找到合适的")
        }
    }

    /**
     * 检查BeanFactory当中的beanName对应的Bean的Qualifier是否匹配?
     *
     * * 1.如果beanName就匹配上了Qualifier的话, 那么直接return true;
     * * 2.如果beanName还没匹配上Qualifier的话, 那么尝试使用beanType/FactoryMethod去进行匹配.
     *
     * @param qualifier Qualifier的匹配断言
     * @param beanName beanName
     * @param beanFactory ListableBeanFactory
     * @return 检查beanName是否和给定的Qualifier断言匹配?
     */
    @JvmStatic
    fun isQualifierMatch(
        qualifier: Predicate<String>,
        beanName: String,
        @Nullable beanFactory: ListableBeanFactory?
    ): Boolean {
        // 1.如果使用qualifier的断言去进行匹配, 就匹配上了的话, 那么return true
        if (qualifier.test(beanName)) {
            return true
        }
        // 如果qualifier断言匹配失败, 但是又没有合适的BeanFactory的话, 那么return false, 因为实在是没有办法匹配了...
        beanFactory ?: return false

        try {
            val beanType = beanFactory.getType(beanName)
            if (beanFactory is ConfigurableBeanFactory) {
                val mbd = beanFactory.getMergedBeanDefinition(beanName)
                if (mbd is RootBeanDefinition) {

                    // 如果能从FactoryMethod上去进行寻找到@Qualifier注解的话, 那么使用Qualifier去进行匹配
                    val factoryMethod = mbd.getResolvedFactoryMethod()
                    if (factoryMethod != null) {
                        val targetAnnotation =
                            AnnotatedElementUtils.getMergedAnnotation(factoryMethod, Qualifier::class.java)
                        if (targetAnnotation != null) {
                            return qualifier.test(targetAnnotation.value)
                        }
                    }
                }
            }
            // 从beanType上去寻找@Qualifier注解, 去进行匹配...
            if (beanType != null) {
                val targetAnnotation = AnnotatedElementUtils.getMergedAnnotation(beanType, Qualifier::class.java)
                if (targetAnnotation != null) {
                    return qualifier.test(targetAnnotation.value)
                }
            }
        } catch (ex: NoSuchBeanDefinitionException) {
            // ignore, wo cannot find candidate bean from bean factory
        }
        return false
    }
}