package com.wanna.boot.autoconfigure.condition

import com.wanna.boot.autoconfigure.AutoConfigurationMetadata
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.context.annotation.ConfigurationCondition
import com.wanna.framework.context.annotation.ConfigurationCondition.ConfigurationPhase
import com.wanna.framework.core.Order
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.type.AnnotatedTypeMetadata
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.core.util.ClassUtils

/**
 * 这是用于匹配Bean的SpringBootCondition，主要处理@ConditionOnBean/@ConditionOnMissingBean/@ConditionOnSingleCandidate等注解
 *
 * @see ConditionOnBean
 * @see ConditionOnMissingBean
 */
@Order(Ordered.ORDER_LOWEST)
@Suppress("UNCHECKED_CAST")
open class OnBeanCondition : FilteringSpringBootCondition(), ConfigurationCondition {
    // 设置它作为Condition应该作用的阶段为注册Bean的阶段
    override fun getConfigurationPhase(): ConfigurationPhase = ConfigurationPhase.REGISTER_BEAN

    /**
     * 匹配configurationClassName.OnBeanCondition/OnSingletonCandidate中配置的className列表，判断是否存在
     */
    override fun getOutcomes(
        autoConfigurationClasses: Array<String?>, autoConfigurationMetadata: AutoConfigurationMetadata
    ): Array<ConditionOutcome?> {
        val outcomes = arrayOfNulls<ConditionOutcome?>(autoConfigurationClasses.size)
        for (index in autoConfigurationClasses.indices) {
            val autoConfigurationClass = autoConfigurationClasses[index]
            if (autoConfigurationClass != null) {  // pass null entry

                // 首先，检查OnBeanCondition的所有className是否都已经存在
                val onBeanTypes = autoConfigurationMetadata.getSet(autoConfigurationClass, "OnBeanCondition")
                outcomes[index] = getOutcome(onBeanTypes, ConditionOnBean::class.java)
                // 如果OnBeanCondition的所有className都已经存在，那么再去检查一下onSingleCandidate的所有className是否都已经存在
                if (outcomes[index] == null) {
                    val onSingletonCandidateTypes =
                        autoConfigurationMetadata.getSet(autoConfigurationClass, "OnSingletonCandidate")
                    outcomes[index] = getOutcome(onSingletonCandidateTypes, ConditionalOnSingleCandidate::class.java)
                }
            }
        }
        return outcomes
    }

    /**
     * 获取Condition的匹配结果，判断requiredBeanTypes列表当中的所有的className是否都已经存在？
     *
     * @param requiredBeanTypes 需要去进行匹配的className列表
     * @return 如果要匹配的所有className都已经存在，那么return null；如果存在有className不存在的，那么return noMatch
     */
    private fun getOutcome(requiredBeanTypes: Set<String>?, annotation: Class<out Annotation>): ConditionOutcome? {
        val missing = filter(requiredBeanTypes, ClassNameFilter.MISSING, this.getClassLoader())
        if (missing.isNotEmpty()) {  // noMatch
            return ConditionOutcome.noMatch()
        }
        return null  // match
    }

    override fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        if (metadata.isAnnotated(ConditionOnBean::class.java.name)) {
            // 构建Spec对象
            val spec = Spec(context, metadata, ConditionOnBean::class.java)
            val matchResult: MatchResult = getMatchBeans(context, spec)
            if (!matchResult.isAllMatch()) {  // 如果不是全部匹配，return false
                return ConditionOutcome.noMatch()
            }
        }
        if (metadata.isAnnotated(ConditionOnMissingBean::class.java.name)) {
            // 构建Spec对象
            val spec = Spec(context, metadata, ConditionOnMissingBean::class.java)
            val matchResult: MatchResult = getMatchBeans(context, spec)
            if (matchResult.isAnyMatch()) {  // 如果有部分匹配，那么return false
                return ConditionOutcome.noMatch()
            }
        }
        // 如果全部都匹配了，return true
        return ConditionOutcome.match()
    }

    private fun getMatchBeans(context: ConditionContext, spec: Spec<*>): MatchResult {
        val result = MatchResult()
        val beanFactory = context.getBeanFactory()!!

        // 1.匹配type
        val types = spec.getTypes()
        types.forEach {
            val beanNames = getBeanNamesForType(beanFactory, it)
            if (beanNames.isEmpty()) {
                result.addUnMatchType(it)
            } else {
                result.addMatchedType(it, beanNames)
            }
        }

        // 2.匹配name
        val names = spec.getNames()
        names.forEach {
            if (containsBean(beanFactory, it)) {
                result.addMatchedName(it)
            } else {
                result.addUnMatchName(it)
            }
        }

        // 3.匹配注解，暂时不支持

        return result
    }

    /**
     * 容器中是否包含了这么个Bean？
     */
    private fun containsBean(beanFactory: ConfigurableListableBeanFactory, name: String): Boolean {
        return beanFactory.containsBeanDefinition(name) || beanFactory.containsSingleton(name)
    }

    /**
     * 根据type从容器当中去获取到beanNames
     */
    private fun getBeanNamesForType(beanFactory: ConfigurableListableBeanFactory, type: String): List<String> {
        return beanFactory.getBeanNamesForType(ClassUtils.forName<Any>(type)) ?: emptyList()
    }

    /**
     * 这是Spring当中对于匹配的结果去进行封装的一个结果，里面维护了各项结果(type/name/annotation等)的匹配结果
     */
    private class MatchResult {
        // 已经匹配的部分
        val matchedNames = ArrayList<String>()
        val matchedTypes = HashMap<String, Collection<String>>()
        val matchedAnnotations = HashMap<String, Collection<String>>()

        // 未匹配的部分
        val unmatchedNames = ArrayList<String>()
        val unmatchedTypes = ArrayList<String>()
        val unmatchedAnnotations = ArrayList<String>()

        /**
         * 添加已经匹配的类型
         */
        fun addMatchedType(type: String, matched: Collection<String>) {
            this.matchedTypes[type] = matched
        }

        /**
         * 添加已经匹配的name
         */
        fun addMatchedName(name: String) {
            this.matchedNames += name
        }

        /**
         * 添加已经匹配的注解
         */
        fun addMatchedAnnotation(annotation: String, matched: Collection<String>) {
            this.matchedAnnotations[annotation] = matched
        }

        /**
         * 添加未匹配的Name
         */
        fun addUnMatchName(name: String) {
            this.unmatchedNames += name
        }

        /**
         * 添加未匹配的类型
         */
        fun addUnMatchType(type: String) {
            this.unmatchedTypes += type
        }

        /**
         * 添加未匹配的注解
         */
        fun addUnMatchAnnotation(annotation: String) {
            this.unmatchedAnnotations += annotation
        }

        /**
         * 是否是只有部分匹配的？(==>是否有部分是未匹配的？)只要unmatch集合中有一个不为空，那么就是部分匹配
         */
        fun isAnyMatch() =
            unmatchedNames.isNotEmpty() || unmatchedTypes.isNotEmpty() || unmatchedAnnotations.isNotEmpty()

        /**
         * 是否全部都匹配了？必须要unmatch的三个集合全部为空才算匹配
         */
        fun isAllMatch() = unmatchedNames.isEmpty() && unmatchedTypes.isEmpty() && unmatchedAnnotations.isEmpty()
    }

    /**
     * 这是Spring当中对于Condition匹配的相关条件的去进行封装的类，它是对于ConditionOnBean/ConditionOnMissingBean的进行的一层抽象
     *
     * @see ConditionOnBean
     * @see ConditionOnMissingBean
     */
    private open class Spec<A : Annotation>(
        context: ConditionContext, metadata: AnnotatedTypeMetadata, annotationType: Class<A>
    ) {
        private val classLoader: ClassLoader = context.getClassLoader()
        private var types: Set<String>? = null
        private var annotations: Set<String>? = null
        private var names: Set<String>? = null

        init {
            // 从metadata当中去解析到AnnotationAttributes信息
            val attributes = metadata.getAnnotationAttributes(annotationType)

            // 1.解析要匹配的beanNames列表
            this.names = (attributes["name"] as Array<String>).toSet()

            // 2.解析要匹配的className类型列表(value属性当中是class，type属性当中是className，合并为className列表)
            val types = HashSet<String>()
            types += (attributes["value"] as Array<Class<*>>).map { it.name }.toList()
            types += (attributes["type"] as Array<String>)
            // 如果从name/value/type属性当中都没有匹配到要匹配的Bean？那么从@Bean方法去推断beanType作为匹配的Bean
            this.types =
                if (types.isEmpty() && this.names!!.isEmpty()) this.deduceBeanType(context, metadata)
                else types

            // 3.解析要匹配的annotations列表
            this.annotations = (attributes["annotation"] as Array<Class<*>>).map { it.name }.toSet()
        }

        open fun getNames(): Set<String> = this.names!!
        open fun getTypes(): Set<String> = this.types!!
        open fun getAnnotations(): Set<String> = this.annotations!!

        /**
         * 推断bean的类型究竟是什么？如果从@Bean方法当中可以推断出来类型，那么就获取返回类型作为要匹配的类型；如果从@Bean方法当中无法推断出来类型，那么return empty
         */
        open fun deduceBeanType(context: ConditionContext, metadata: AnnotatedTypeMetadata): Set<String> {
            if (metadata is MethodMetadata && metadata.isAnnotated(Bean::class.java.name)) {
                return deduceBeanTypeFromBeanMethod(context, metadata)
            }
            return emptySet()
        }

        /**
         * 从@Bean方法当中去推断Bean的类型
         */
        open fun deduceBeanTypeFromBeanMethod(context: ConditionContext, metadata: MethodMetadata): Set<String> {
            return setOf(metadata.getReturnTypeName())
        }
    }
}