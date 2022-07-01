package com.wanna.framework.beans.factory.support

/**
 * 这是Spring中的Autowire的候选Bean的解析器，主要作用是，通过DependencyDescriptor去判断一个依赖和要进行注入的类型是否匹配；
 * 它支持去处理@Value/@Qualifier/@Lazy注解的处理，Spring BeanFactory去进行Autowire时，就会使用AutowireCandidateResolver去
 * 辅助BeanFactory去利用DependencyDescriptor去完成依赖的解析工作
 *
 * @see DependencyDescriptor
 * @see QualifierAnnotationAutowireCandidateResolver
 * @see ContextAnnotationAutowireCandidateResolver
 */
interface AutowireCandidateResolver {

    /**
     * 判断一个Bean是否是符合进行注入的要求？默认只从BeanDefinition.isAutowireCandidate中进行判断；
     *
     * ## 1.DependencyDescriptor上有Qualifier注解的情况
     * Spring在进行Autowire的匹配时，会将所有的候选Bean的列表，挨个调用这个方法去进行匹配，而BeanDefinition就是该候选的Bean的相关信息；
     * (子类中)首先会比较DependencyDescriptor当中的Qualifier和候选的BeanDefinition中的Qualifier是否**成对**匹配？匹配则return true；
     * 如果不匹配的话，那么就尝试将Qualifier注解当中的value属性和bdHolder.beanName去进行匹配，如果匹配的话那么return true
     *
     * ## 2.DependencyDescriptor上没有Qualifier注解的情况
     * 只要类型匹配时，那么就return true，不用去进行beanName的匹配
     *
     * @param bdHolder candidate BeanDefinition and beanName
     * @param descriptor 依赖描述符
     * @return 该Bean是否是Autowire时的候选Bean，如果return false那么该Bean不是候选的
     */
    fun isAutowireCandidate(bdHolder: BeanDefinitionHolder, descriptor: DependencyDescriptor): Boolean {
        return bdHolder.beanDefinition.isAutowireCandidate()
    }

    /**
     * 判断这个依赖是否是必须进行注入的，默认实现为只根据DependencyDescriptor去进行决定；
     * 在子类当中可以去进行扩展，去实现比如在@Autowired注解上去找是否required=true
     *
     * @param descriptor 依赖描述符
     * @return 是否是必须的？如果是必须的，但是没有Spring BeanFactory找到直接抛异常
     */
    fun isRequired(descriptor: DependencyDescriptor): Boolean {
        return descriptor.isRequired()
    }

    /**
     * 判断是否有Qualifier限定符，不仅包括Spring自家的@Qualifier注解，也包括javax.inject当中的Qualifier注解；
     * 遍历依赖描述符上的所有的注解，去和Qualifier去进行比对，如果匹配那么return true；不然return false
     *
     * @param descriptor 要进行匹配的依赖描述符
     * @return 该依赖描述符上是否有Qualifier？
     */
    fun hasQualifier(descriptor: DependencyDescriptor): Boolean {
        return false
    }

    /**
     * 决定是否有一个建议去进行设置的默认值，用来处理字段或者方法参数上标注的@Value注解
     *
     * @param descriptor 要去进行注入的依赖描述符
     * @return 是否有默认的建议值？如果没有return null，有建议返回值的默认值则返回
     */
    fun getSuggestedValue(descriptor: DependencyDescriptor): Any? {
        return null
    }

    /**
     * 如果必要的话，创建一个完成解析懒加载的注入依赖的代理对象(@Autowired时，可以使用@Lazy注解去进行懒加载就是这个原理)；
     * Note: 可以自行去构建一个TargetSource，实现每次调用目标方法时，都能在TargetSource当中去获取真正的对象来执行目标方法，
     * 每次执行目标对象的方法时，都能自动从容器当中解析依赖去完成，而不是一开始就注入完成，后期不能变更，比如可以用来实现注入原型Bean
     *
     * @param descriptor 依赖描述符
     * @param beanName beanName
     * @return 构建好的Lazy代理对象(如果lazy=false，那么return null)
     */
    fun getLazyResolutionProxyIfNecessary(descriptor: DependencyDescriptor, beanName: String?): Any? {
        return null
    }
}