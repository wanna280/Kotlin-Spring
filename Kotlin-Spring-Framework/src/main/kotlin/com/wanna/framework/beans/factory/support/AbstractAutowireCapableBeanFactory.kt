package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.BeanWrapper
import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.factory.support.definition.AbstractBeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.MutablePropertyValues
import com.wanna.framework.beans.PropertyValues
import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.ObjectFactory
import com.wanna.framework.beans.BeanWrapperImpl
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.aware.BeanNameAware
import com.wanna.framework.context.exception.BeanCreationException
import com.wanna.framework.context.exception.BeansException
import com.wanna.framework.core.DefaultParameterNameDiscoverer
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.ParameterNameDiscoverer
import com.wanna.framework.core.util.BeanUtils
import com.wanna.framework.core.util.ReflectionUtils
import com.wanna.framework.core.util.StringUtils
import java.beans.Introspector
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.function.Supplier

/**
 * 这是一个拥有Autowire能力的BeanFactory，不仅提供了普通的BeanFactory的能力，也可以提供createBean等Autowire相关工作
 */
abstract class AbstractAutowireCapableBeanFactory : AbstractBeanFactory(), AutowireCapableBeanFactory {

    // 是否开启了循环依赖？默认设置为true
    private var allowCircularReferences: Boolean = true

    /**
     * 指定Bean的实例化策略，目前支持的策略，包括简单的策略/Cglib的策略
     *
     * @see CglibSubclassingInstantiationStrategy
     * @see SimpleInstantiationStrategy
     */
    private var instantiationStrategy: InstantiationStrategy = CglibSubclassingInstantiationStrategy()

    // 参数名发现器，支持去进行方法/构造器的参数名列表的获取
    private var parameterNameDiscoverer: ParameterNameDiscoverer? = DefaultParameterNameDiscoverer()

    /**
     * 给定beanName和BeanDefinition，去完成Bean的创建
     *
     * @param beanName beanName
     * @param mbd MergedBeanDefinition
     * @return 创建好的Bean，有可能为NullBean
     */
    override fun createBean(beanName: String, mbd: RootBeanDefinition): Any {
        // 在实例化之前，交给有资格对实例化去进行干涉的BeanPostProcessor(InstantiationAwareBeanPostProcessor)去进行回调
        // 让它们去进行干涉，如果它们可以成功return对象，那么说明它们创建对象成功，直接return即可，不用去执行后续的创建Bean的过程了
        try {
            val bean = resolveBeforeInstantiation(beanName, mbd)
            if (bean != null) {
                return bean
            }
        } catch (ex: Throwable) {
            throw BeanCreationException("在执行BeforeInstantiation的过程中发生了异常", ex, beanName)
        }
        return doCreateBean(beanName, mbd)
    }

    /**
     * 给定一个beanClass，封装成为一个BeanDefinition，交给容器当中去创建Bean
     *
     * @param clazz beanClass
     * @return 创建好的Bean
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> createBean(clazz: Class<T>): T {
        val rootBeanDefinition = RootBeanDefinition(clazz)
        rootBeanDefinition.setScope(BeanDefinition.SCOPE_PRTOTYPE)
        return createBean(clazz.name, rootBeanDefinition) as T
    }

    /**
     * 在对Bean进行实例化之前，尝试从BeanPostProcessor当中去推断出来一个合适的Bean
     *
     * @param beanName
     * @param mbd MergedBeanDefinition
     * @return 如果推断出来了Bean，return Bean；不然return null
     */
    protected open fun resolveBeforeInstantiation(beanName: String, mbd: RootBeanDefinition): Any? {
        var bean: Any? = null

        // 判断有没有可能在初始化之前解析到Bean？最初被初始化为true，如果第一次解析的时候为bean=null，那么设为false，后续就不用再去进行解析了
        if (!mbd.beforeInstantiationResolved) {
            val beanClass = mbd.getBeanClass()
            if (beanClass != null) {
                // 如果实例之前的BeanPostProcessor已经return 非空，产生出来一个对象了，那么需要完成初始化工作...
                // 如果必要的话，会完成动态代理，如果创建出来Bean，那么直接return，就不走doCreateBean的创建Bean的逻辑了...
                for (postProcessor in getBeanPostProcessorCache().instantiationAwareCache) {
                    bean = postProcessor.postProcessBeforeInstantiation(beanName, beanClass)
                    if (bean != null) {
                        bean = applyBeanPostProcessorsAfterInitialization(bean, beanName)
                    }
                }
            }
            mbd.beforeInstantiationResolved = bean != null
        }
        return bean
    }

    /**
     * doCreateBean，去执行真正的Bean的实例化和初始化工作
     *
     * @throws BeanCreationException 如果在doGetBean的过程当中发生了异常
     */
    protected open fun doCreateBean(beanName: String, mbd: RootBeanDefinition): Any {
        val beanWrapper = createBeanInstance(beanName, mbd)
        val beanInstance = beanWrapper.getWrappedInstance()
        val beanType = beanWrapper.getWrappedClass()

        val earlySingletonExposure =
            mbd.isSingleton() && allowCircularReferences && isSingletonCurrentlyInCreation(beanName)
        // 如果设置了允许早期引用，那么将Bean放入到三级缓存当中...
        if (earlySingletonExposure) {
            // 添加到SingletonFactory当中，ObjectFactory当中包装的是一getEarlyReference，当从SingletonFactory中获取对象时
            // 会自动回调getEarlyReference方法完成对象的创建
            addSingletonFactory(beanName, object : ObjectFactory<Any> {
                override fun getObject() = getEarlyReference(beanInstance, beanName)
            })
        }

        synchronized(mbd.postProcessLock) {
            if (!mbd.postProcessed) {
                // 在Bean实例化之后，可以获取到BeanClass的真正类型，可以去完成BeanDefinition的Merged工作
                // 给BeanPostProcessor一个机会，让它可以将parent BeanDefinition中的属性可以合并到当前的BeanDefinition当中
                try {
                    applyMergedBeanDefinitionPostProcessor(mbd, beanType, beanName)
                } catch (ex: Throwable) {
                    throw BeanCreationException("完成merged的后置处理工作失败，[beanName=$beanName]", ex)
                }
                mbd.postProcessed = true
            }
        }

        var exposedBean: Any = beanInstance
        try {
            // 填充Bean的属性
            populateBean(beanWrapper, mbd, beanName)

            // 初始化Bean
            exposedBean = initializeBean(exposedBean, beanName, mbd)
        } catch (ex: Throwable) {
            if (ex is BeanCreationException) {
                throw ex
            }
            // 如果只是一个普通异常，那么需要去对ex进行再一次包装，往上抛
            throw BeanCreationException("初始化Bean失败", ex, beanName)
        }

        // 这是Spring解决循环依赖的很关键的一步，将exposedBean设置为getSingleton获取到的earlySingletonReference
        // 因为在A注入B过程当中出现了循环依赖，如果A需要被代理，那么getEarlyReference可以保证放进最开始放入缓存的确实是代理对象A'
        // 但是这里，在A完成注入和初始化之后，返回的exposedBean，并不是代理对象A'，而是未代理的对象A；因此我们应该从缓存当中获取早期引用A'作为真实的Bean
        // 而不是使用最开始的exposedBean作为要去进行使用的Bean，不然有可能出现，把未完成代理的对象加入到缓存当中去覆盖了之前的已经完成代理的对象...

        // 为什么说，完成注入和初始化的是A对象，而不是A'对象？因为A'是调用getEarlyReference去生成的，我原来的A的操作是不受任何的影响的(除了A不会在初始化过程中生成代理)，
        // 因此注入和初始化都是操作的A对象，而不是A'对象；那么，既然代理对象没有完成注入和初始化，代理对象是否是半成品对象，导致最终的运行结果不正确？
        // 不会！因为创建代理时，将A包装到TargetSource里了，而在运行时调用代理方法，都是通过TargetSource.getTarget去获取到的A对象去进行委托完成，而不是使用A'去进行的操作；
        // 也就是说，在代理对象内调用this，其实获取到的是A对象，而不是代理对象A'，这也是为什么在@Transational方法里调用this.XXX(也是一个@Transactional方法)时不生效的原因
        if (earlySingletonExposure) {
            val earlySingletonReference = getSingleton(beanName, false)
            if (earlySingletonReference != null) {
                // 如果exposedBean == bean，那么要使用的exposedBean，应该是早期引用，而不是原始的Bean，因此这里需要替换exposedBean
                if (exposedBean == beanInstance) {
                    exposedBean = earlySingletonReference
                }
            }
        }

        // registerDisposableBeanIfNecessary，注册destroy的回调函数
        registerDisposableBeanIfNecessary(beanName, exposedBean, mbd)
        return exposedBean
    }

    /**
     * 选择合适的方式去创建真正的Bean实例，并包装成为一个BeanWrapper的包装对象
     * (1)如果提供了InstanceSupplier，那么从Supplier当中去进行获取Bean
     * (2)如果指定了factoryMethodName，那么从factoryMethod当中去进行获取Bean(@Bean方法)
     */
    protected open fun createBeanInstance(
        beanName: String, mbd: RootBeanDefinition, args: Array<Any?>?
    ): BeanWrapper {
        val beanClass = mbd.getBeanClass()

        // 1.如果指定了自定义的实例化Supplier的话
        if (mbd.getInstanceSupplier() != null) {
            return obtainFromInstanceSupplier(mbd.getInstanceSupplier()!!, beanName)
        }
        // 2.如果指定了FactoryMethod的话，那么使用FactoryMethod去进行实例化(本质上也是看做构造器的实例化方式)
        if (mbd.getFactoryMethodName() != null) {
            return instantiateUsingFactoryMethod(beanName, mbd)
        }

        var resolved = false  // 是否已经完成了Constructor解析工作？
        var autowireIfNecessary = false  // 是否需要进行Autowire？判断是否有解析出来的参数即可判断

        // 如果没有给定具体的参数，那么可以尝试先去缓存当中获取，如果给定了具体的参数，那么肯定就不能走缓存了...
        if (args == null) {
            synchronized(mbd.constructorArgumentLock) {
                if (mbd.resolvedConstructorOrFactoryMethod != null) {
                    resolved = true
                    autowireIfNecessary = mbd.constructorArgumentsResolved
                }
            }
        }

        // 如果该BeanDefinition已经完成过构造器的解析了，那么直接使用构造器去进行实例化即可
        if (resolved) {
            // 如果解析的结果当中有参数的话，需要进行Autowire
            // 如果解析的结果当中没有参数的话，那么直接使用无参数构造器去进行实例化即可
            if (autowireIfNecessary) {
                return autowireConstructor(beanName, mbd, emptyArray(), emptyArray())
            } else {
                return instantiate(beanName, mbd)
            }
        }

        // 如果没有解析过的话，那么需要从所有的BeanPostProcessor当中去推断合适的构造器列表
        val ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName)

        // (1)如果推断出来合适的构造器的话;(2)BeanDefinition当中有构造器参数;(3)注入模式为构造器注入;(4)传入进来了合适的参数列表;
        // 那么采用构造器注入的方式去完成Bean的实例化
        if (ctors != null || mbd.hasConstructorArgumentValues() || mbd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR || (args != null && args.isNotEmpty())) {
            return autowireConstructor(beanName, mbd, ctors, args)
        }

        // 没有找到合适的构造器，那么尝试去采用默认的方式去完成实例化
        return instantiate(beanName, mbd)
    }


    protected open fun createBeanInstance(beanName: String, mbd: RootBeanDefinition): BeanWrapper {
        return createBeanInstance(beanName, mbd, null)
    }

    /**
     * 遍历所有的BeanPostProcessor，去推断出来合适的构造器去完成Bean的初始化
     * @return 如果没有推断出来合适的构造器，那么return null；如果推断出来了，return推断出来的构造器列表
     */
    private fun determineConstructorsFromBeanPostProcessors(
        beanClass: Class<*>?, beanName: String
    ): Array<Constructor<*>>? {
        if (beanClass != null && getBeanPostProcessorCache().hasSmartInstantiationAware()) {
            getBeanPostProcessorCache().smartInstantiationAwareCache.forEach {
                val candidateConstructors = it.determineCandidateConstructors(beanClass, beanName)
                if (candidateConstructors != null) {
                    return candidateConstructors
                }
            }
        }
        return null
    }

    /**
     * 使用默认的方式去进行实例化
     */
    private fun instantiate(beanName: String, mbd: RootBeanDefinition): BeanWrapper {
        val instance = getInstantiationStrategy().instantiate(mbd, beanName, this)
        val beanWrapper = BeanWrapperImpl(instance)
        initBeanWrapper(beanWrapper)
        return beanWrapper
    }

    /**
     * 从实例化的InstanceSupplier当中去获取对象，并封装成为BeanWrapper
     */
    private fun obtainFromInstanceSupplier(supplier: Supplier<*>, beanName: String): BeanWrapper {
        var instance = supplier.get()
        if (instance == null) {  // 如果从实例化的Supplier当中获取到了null，那么封装NullBean
            instance = NullBean()
        }
        val beanWrapper = BeanWrapperImpl(instance)
        initBeanWrapper(beanWrapper)
        return beanWrapper
    }

    /**
     * 通过构造器完成Bean的实例化，并完成参数的自动注入
     *
     * @param ctors 推断出来的构造器列表，可以为空；交给ConstructorResolver去进行解析
     * @param args 构造器参数，可以为空(进行自动注入)
     */
    private fun autowireConstructor(
        beanName: String, mbd: RootBeanDefinition, ctors: Array<Constructor<*>>?, args: Array<out Any?>?
    ): BeanWrapper {
        return ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, args)
    }

    /**
     * 使用FactoryMethod去完成Bean的实例化工作，需要使用到Constructor去协助完成，最终也是通过实例化策略去对Bean去进行实例化的
     *
     * @param beanName beanName
     * @param mbd MergedBeanDefinition
     * @return beanWrapper
     */
    private fun instantiateUsingFactoryMethod(beanName: String, mbd: RootBeanDefinition): BeanWrapper {
        return ConstructorResolver(this).instantiateUsingFactoryMethod(beanName, mbd)
    }


    /**
     * 应用所有的MergedBeanDefinitionPostProcessor，去完成BeanDefinition的合并，此时得到的beanType为实例化之后得到的对象的真实beanType
     */
    private fun applyMergedBeanDefinitionPostProcessor(mbd: RootBeanDefinition, beanType: Class<*>, beanName: String) {
        if (getBeanPostProcessorCache().hasMergedDefinition()) {
            getBeanPostProcessorCache().mergedDefinitions.forEach {
                it.postProcessMergedBeanDefinition(mbd, beanType, beanName)
            }
        }
    }

    /**
     * 对Bean去完成初始化，执行Bean的初始化回调方法，以及对Bean的初始化前后的去进行干涉的BeanPostProcessor
     *
     * @param beanName beanName
     * @param bean 要去进行初始化的Bean
     * @param mbd MergedBeanDefinition
     * @throws BeanCreationException 执行初始化过程当中发生了异常
     * @throws Throwable 在执行初始化之前/之后的方法当中，发生的异常将会直接往上抛
     */
    protected open fun initializeBean(bean: Any, beanName: String, mbd: RootBeanDefinition?): Any {
        // 在初始化之前，需要去执行Aware接口当中的setXXX方法去注入相关的容器对象，beanName和beanFactory是需要这里去完成的，别的类型的Aware接口
        // 就交给ApplicationContextAwareBeanPostProcessor去完成，因为ApplicationContextAware能获取更多对象，比如Environment
        invokeAwareMethods(bean, beanName)

        // 执行初始化之前的方法(BeforeInitialization)，应用所有的对Bean的初始化进行干涉的BeanPostProcessor
        var wrappedBean: Any = applyBeanPostProcessorsBeforeInitialization(bean, beanName)


        // 执行初始化方法，如果在执行初始化过程当中发生了异常，那么把异常包装成为BeanCreationException抛出去
        try {
            invokeInitMethod(wrappedBean, beanName, mbd)
        } catch (ex: Throwable) {
            throw BeanCreationException("执行对Bean的初始化过程中出现了异常", ex, beanName)
        }

        // 执行初始化之后的方法(AfterInitialization)，应用所有的对Bean的初始化进行干涉的BeanPostProcessor
        wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName)
        return wrappedBean
    }

    /**
     * 在完成Bean的实例化之后，需要去完成一个Bean的属性值填充
     */
    private fun populateBean(wrapper: BeanWrapper, mbd: RootBeanDefinition, beanName: String) {
        // 在实例化之后，应用所有的干涉Bean的实例化的处理器(InstantiationAwareBeanPostProcessor)
        if (getBeanPostProcessorCache().hasInstantiationAware()) {
            // 执行实例化之后的BeanPostProcessor
            for (postProcessor in getBeanPostProcessorCache().instantiationAwareCache) {
                if (!postProcessor.postProcessAfterInstantiation(beanName, wrapper.getWrappedInstance())) {
                    return
                }
            }
        }
        // 如果beanDefinition当中有属性值，那么获取属性值，如果beanDefinition当中没有属性值，应该return null；但是对于BeanDefinition来说应该返回的是一个空的MutablePropertyValue
        var pvs: PropertyValues? = if (mbd.hasPropertyValues()) mbd.getPropertyValues() else null

        val resolvedAutowireMode = mbd.getAutowireMode()
        // 如果解析到的AutowireMode为byName或者是byType去进行注入的话，那么需要解析相关的依赖，并放入到pvs当中
        // 这是很有用的，本来是应用在XML的Spring当中的，但是就算是在注解版Spring当中，它也非常重要，它能支持去寻找所有的Setter，并将其添加到pvs当中
        if (resolvedAutowireMode == AbstractBeanDefinition.AUTOWIRE_BY_NAME || resolvedAutowireMode == AbstractBeanDefinition.AUTOWIRE_BY_TYPE) {
            val newPvs = MutablePropertyValues(pvs)  // copy PropertyValues
            if (resolvedAutowireMode == AbstractBeanDefinition.AUTOWIRE_BY_NAME) {
                autowireByName(beanName, mbd, wrapper, newPvs)
            }
            if (resolvedAutowireMode == AbstractBeanDefinition.AUTOWIRE_BY_TYPE) {
                autowireByType(beanName, mbd, wrapper, newPvs)
            }
            pvs = newPvs  // 使用newPvs替换之前的pvs，作为要去进行使用的pvs
        }

        // 如果必要的话，遍历所有的BeanPostProcessor，去进行Autowire自动注入的处理...
        // 它有可能会涉及到pvs的移除，因为有些BeanPostProcessor它已经完成注入了，就不必再次使用属性值去进行注入了...
        if (getBeanPostProcessorCache().hasInstantiationAware()) {
            // 完成Bean的属性填充
            for (postProcessor in getBeanPostProcessorCache().instantiationAwareCache) {
                pvs = postProcessor.postProcessProperties(pvs, wrapper.getWrappedInstance(), beanName)
            }
        }

        // 如果有PropertyValues的话，那么需要进行应用PropertyValues到BeanWrapper当中的beanInstance
        // 针对于byName或者byType的方式去进行自动注入的情况，就会往PropertyValues当中添加内容(别的地方也需要用到PropertyValue的设置)
        // 因此就需要将该值应用给BeanWrapper当中的beanInstance当中
        if (pvs != null) {
            applyPropertyValues(beanName, mbd, wrapper, pvs)
        }
    }

    /**
     * 应用所有的PropertyValues到BeanWrapper当中，可以通过PropertyValue去对Bean的某些字段值去进行设置
     *
     * @param pvs PropertyValues
     * @param mbd MergedBeanDefinition
     * @param beanWrapper beanWrapper
     * @param beanName beanName
     */
    protected open fun applyPropertyValues(
        beanName: String, mbd: RootBeanDefinition, beanWrapper: BeanWrapper, pvs: PropertyValues
    ) {

        val beanDefinitionValueResolver = BeanDefinitionValueResolver(this, beanName, mbd, beanWrapper)

        pvs.getPropertyValues().forEach { pv ->
            // BeanDefinition的值解析器，需要解析比如RuntimeBeanReference等多种的情况
            val resolvedValue = beanDefinitionValueResolver.resolveValueIfNecessary(pv, pv.value)
            pv.value = resolvedValue
        }

        try {
            // 通过beanWrapper去设置propertyValues
            beanWrapper.setPropertyValues(pvs)
        } catch (ex: BeansException) {
            throw BeanCreationException("给beanName=[$beanName]的Bean去进行属性赋值的过程当中出现了异常")
        }
    }

    /**
     * 通过byName的方式去进行自动注入，需要解析所有的非简单属性的setter，将其添加到pvs当中
     *
     * @param name beanName
     * @param mbd MergedBeanDefinition
     * @param beanWrapper beanWrapper
     * @param pvs PropertyValues
     *
     * @see AbstractBeanDefinition.AUTOWIRE_BY_NAME
     * @see AbstractBeanDefinition.setAutowireMode
     */
    protected open fun autowireByName(
        name: String, mbd: RootBeanDefinition, beanWrapper: BeanWrapper, pvs: MutablePropertyValues
    ) {
        val propertyNames = unsatisfiedNonSimpleProperties(mbd, beanWrapper)
        propertyNames.forEach { (propertyName, _) ->
            if (containsBeanDefinition(propertyName)) {
                val bean = getBean(propertyName)
                pvs.addPropertyValue(propertyName, bean)
            }
        }
    }

    /**
     * 通过byType的方式去进行自动注入，需要解析所有的非简单属性的setter，将其添加到pvs当中
     *
     * Note: Autowired注入时，eager=true，必须去进行注入，就算是FactoryBean也得给我注入进来
     *
     * @param beanName beanName
     * @param mbd MergedBeanDefinition
     * @param beanWrapper beanWrapper
     * @param pvs PropertyValues
     *
     * @see AbstractBeanDefinition.AUTOWIRE_BY_TYPE
     * @see AbstractBeanDefinition.setAutowireMode
     */
    protected open fun autowireByType(
        beanName: String, mbd: RootBeanDefinition, beanWrapper: BeanWrapper, pvs: MutablePropertyValues
    ) {
        val propertyNames = unsatisfiedNonSimpleProperties(mbd, beanWrapper)
        propertyNames.forEach { (propertyName, method) ->
            val dependency = resolveDependency(DependencyDescriptor(MethodParameter(method, 0), false, true), beanName)
            if (dependency != null) {
                pvs.addPropertyValue(propertyName, dependency)
            }
        }
    }

    /**
     * 获取非简单属性的列表
     *
     * @param mbd BeanDefinition
     * @param bw BeanWrapper
     */
    protected open fun unsatisfiedNonSimpleProperties(
        mbd: AbstractBeanDefinition,
        bw: BeanWrapper
    ): Map<String, Method> {
        val result = HashMap<String, Method>()
        val propertyValues = mbd.getPropertyValues()
        ReflectionUtils.doWithMethods(bw.getWrappedClass()) {
            if (it.name.startsWith("set") && it.parameterCount == 1 && !BeanUtils.isSimpleProperty(it.parameterTypes[0])) {
                val propertyName = Introspector.decapitalize(it.name.substring(3))
                if (!propertyValues.containsProperty(propertyName)) {
                    result += propertyName to it
                }
            }
        }
        return result
    }

    /**
     * 执行Init方法完成初始化
     */
    private fun invokeInitMethod(bean: Any, beanName: String, mbd: RootBeanDefinition?) {
        // 如果它是一个InitializingBean，那么需要在这里去进行回调去完成Bean的初始化
        if (bean is InitializingBean) {
            bean.afterPropertiesSet()
        }
        val beanClass = bean::class.java
        // 如果beanDefinition当中设置了initMethodName的话，那么需要获取该方法去执行
        if (mbd != null && beanClass != NullBean::class.java && StringUtils.hasText(mbd.getInitMethodName())) {
            val initMethod = beanClass.getMethod(mbd.getInitMethodName()!!)
            ReflectionUtils.makeAccessible(initMethod)
            ReflectionUtils.invokeMethod(initMethod, bean)
        }
    }

    /**
     * 执行Aware，主要是BeanNameAware和BeanFactoryAware、BeanClassLoaderAware，别的Aware会在ApplicationContextAwareProcessor当中进行处理
     */
    private fun invokeAwareMethods(bean: Any, beanName: String) {
        if (bean is BeanNameAware) {
            bean.setBeanName(beanName)
        }
        if (bean is BeanFactoryAware) {
            bean.setBeanFactory(this)
        }
        if (bean is BeanClassLoaderAware) {
            bean.setBeanClassLoader(this.getBeanClassLoader())
        }
    }

    /**
     * 获取Bean的早期引用的回调，如果必要的话，会在这里去进行生成代理
     */
    protected open fun getEarlyReference(bean: Any, beanName: String): Any {
        var result = bean
        // 遍历所有的SmartInstantiationAware的BeanPostProcessor的getEarlyReference方法
        // 如果必要的话，会在这里完成AOP动态代理
        for (postProcessor in getBeanPostProcessorCache().smartInstantiationAwareCache) {
            result = postProcessor.getEarlyReference(bean, beanName)
        }
        return result
    }

    /**
     * 执行beforeInitialization方法，如果必要的话创建代理；
     * <note>如果其中一个BeanPostProcessor执行过程当中return null的话，终止后续的BeanPostProcessor的执行，直接return result；
     * 如果BeanPostProcessor返回的不为null的话，那么将result=current，去进行继续执行</note>
     */
    protected open fun applyBeanPostProcessorsBeforeInitialization(bean: Any, beanName: String): Any {
        var result: Any = bean
        for (postProcessor in beanPostProcessors) {
            val current = postProcessor.postProcessBeforeInitialization(beanName, result) ?: return result
            result = current
        }
        return result
    }

    /**
     * 执行afterInitialization方法，如果必要的话创建代理
     * <note>如果其中一个BeanPostProcessor执行过程当中return null的话，终止后续的BeanPostProcessor的执行，直接return result；
     * 如果BeanPostProcessor返回的不为null的话，那么将result=current，去进行继续执行</note>
     */
    protected open fun applyBeanPostProcessorsAfterInitialization(bean: Any, beanName: String): Any {
        var result = bean
        for (postProcessor in this.beanPostProcessors) {
            val current = postProcessor.postProcessAfterInitialization(beanName, result) ?: return result
            result = current
        }
        return result
    }

    /**
     * 获取实例化策略，提供对无参构造器/FactoryMethod/以及指定的候选构造器等多种方式对Bean去进行实例化的方式
     */
    open fun getInstantiationStrategy(): InstantiationStrategy = this.instantiationStrategy

    /**
     * 获取BeanFactory当中的参数名发现器，可以去完成参数名的发现
     *
     * @see ParameterNameDiscoverer
     */
    open fun getParameterNameDiscoverer(): ParameterNameDiscoverer? = this.parameterNameDiscoverer

    /**
     * 设置Spring BeanFactory的参数名发现器
     */
    open fun setParameterNameDiscovery(parameterNameDiscoverer: ParameterNameDiscoverer?) {
        this.parameterNameDiscoverer = parameterNameDiscoverer
    }

    /**
     * 是否允许循环引用？
     */
    open fun isAllowCircularReferences(): Boolean = this.allowCircularReferences

    /**
     * 是否是否允许循环引用？
     */
    open fun setAllowCircularReferences(allowCircularReferences: Boolean) {
        this.allowCircularReferences = allowCircularReferences
    }

    /**
     * 对一个Bean去完成初始化，供beanFactory外部去进行使用
     *
     * @param beanName beanName
     * @param bean bean
     */
    override fun initializeBean(bean: Any, beanName: String) {
        initializeBean(bean, beanName, null)
    }

    /**
     * 摧毁一个Bean，回调的它的destory方法，供beanFactory外部去进行使用
     *
     * @param existingBean 要进行摧毁的已经存在于容器当中的Bean
     */
    override fun destroy(existingBean: Any) {
        DisposableBeanAdapter(
            existingBean, existingBean::class.java.name, null, getBeanPostProcessorCache().destructionAwareCache
        ).destroy()
    }
}