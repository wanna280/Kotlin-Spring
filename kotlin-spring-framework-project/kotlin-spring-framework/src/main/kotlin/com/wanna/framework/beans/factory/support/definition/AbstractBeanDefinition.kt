package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.beans.MutablePropertyValues
import com.wanna.framework.beans.factory.config.ConstructorArgumentValues
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import com.wanna.framework.beans.factory.support.definition.BeanDefinition.Companion.ROLE_APPLICATION
import com.wanna.framework.beans.factory.support.definition.BeanDefinition.Companion.SCOPE_PROTOTYPE
import com.wanna.framework.beans.factory.support.definition.BeanDefinition.Companion.SCOPE_SINGLETON
import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataAttributeAccessor
import com.wanna.framework.beans.method.MethodOverrides
import com.wanna.framework.core.io.Resource
import com.wanna.framework.lang.Nullable
import java.util.function.Supplier

/**
 * 这是一个抽象的BeanDefinition，它继承了BeanMetadataAttributeAccessor，支持进行属性的设置和获取
 */
abstract class AbstractBeanDefinition : BeanDefinition,
    BeanMetadataAttributeAccessor() {
    companion object {
        const val DEFAULT_SCOPE = ""
        const val AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO
        const val AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE
        const val AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME
        const val AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR
    }

    /**
     * copy对象
     *
     * @param origin 原始的BeanDefinition
     * @param target 要去拷贝到目标BeanDefinition的对象
     */
    open fun <T : BeanDefinition> copy(@Nullable origin: BeanDefinition?, target: T) {
        origin ?: return
        target.setBeanClass(origin.getBeanClass())
        target.setAbstract(origin.isAbstract())
        target.setPrimary(origin.isPrimary())
        target.setInitMethodName(origin.getInitMethodName())
        target.setDestroyMethodName(origin.getDestroyMethodName())
        target.setScope(origin.getScope())
        target.setRole(origin.getRole())
        target.setInstanceSupplier(origin.getInstanceSupplier())
        target.setFactoryBeanName(origin.getFactoryBeanName())
        target.setFactoryMethodName(origin.getFactoryMethodName())
        target.setLazyInit(origin.isLazyInit())
        target.setAutowireCandidate(origin.isAutowireCandidate())
        if (origin is AbstractBeanDefinition && target is AbstractBeanDefinition) {
            target.setBeanClassName(origin.beanClassName)
            target.setSource(origin.getSource())
            target.setSynthetic(origin.synthetic)
            target.setAutowireMode(origin.getAutowireMode())
            target.dependsOn = origin.getDependsOn()
            target.methodOverrides = MethodOverrides(origin.getMethodOverrides())
            target.propertyValues = MutablePropertyValues(MutablePropertyValues())
            target.setSource(origin.getSource())
            target.setDescription(origin.getDescription())
            target.setResource(origin.getResource())
            target.constructorArgumentValues = ConstructorArgumentValues(origin.getConstructorArgumentValues())
            this.getPropertyValues().addPropertyValues(origin.getPropertyValues().getPropertyValues().toList())
        }
    }

    /**
     * beanClassName
     */
    @Nullable
    private var beanClassName: String? = null

    /**
     * beanClass
     */
    @Nullable
    private var beanClass: Class<*>? = null

    /**
     * 在进行autowire时，它是否是优先注入的Bean？
     */
    private var primary = false

    /**
     * 该BeanDefinition是否是一个合成的BeanDefinition? 如果为true, 代表它不会经过AOP的自动代理
     */
    private var synthetic = false

    /**
     * 初始化回调方法的name
     */
    @Nullable
    private var initMethodName: String? = null

    /**
     * destroy的回调方法name
     */
    @Nullable
    private var destroyMethodName: String? = null

    /**
     * Bean所处的作用域，比如singleton/prototype
     */
    private var scope = DEFAULT_SCOPE

    /**
     * Bean所处的角色，Application(0)、Support(1)和Infrastructure(2)
     */
    private var role = ROLE_APPLICATION

    /**
     * 它是否是抽象的？
     */
    private var abstractFlag = false

    /**
     * 实例化Bean的Supplier
     */
    @Nullable
    private var instanceSupplier: Supplier<*>? = null

    /**
     * 工厂方法(@Bean方法)的方法的name
     */
    @Nullable
    private var factoryMethodName: String? = null

    /**
     * 工厂方法(@Bean方法)所在类的Bean的beanName
     */
    @Nullable
    private var factoryBeanName: String? = null

    /**
     * autowire的模式，BY_TYPE/BY_NAME/CONSTRUCTOR
     */
    private var autowireMode = AUTOWIRE_NO

    /**
     * 是否是一个候选去进行autowire的Bean
     */
    private var autowireCandidate = true

    /**
     * 是否懒加载，默认为false
     */
    private var lazyInit = false

    /**
     * Bean所依赖的Bean的列表, 设置的是依赖的BeanName
     */
    private var dependsOn: Array<String> = emptyArray()

    /**
     * 运行时方法重写的列表
     */
    private var methodOverrides = MethodOverrides()

    /**
     * 要对Bean进行设置的属性值列表, 在运行时将会完成属性值的自动注入
     */
    private var propertyValues = MutablePropertyValues()

    /**
     * Bean的构造器参数列表, 在运行时将会根据构造器参数列表完成属性值的自动注入
     */
    private var constructorArgumentValues = ConstructorArgumentValues()

    /**
     * 解析到BeanDefinition的Resource
     */
    @Nullable
    private var resource: Resource? = null

    /**
     * BeanDefinition的描述信息
     */
    @Nullable
    private var description: String? = null

    /**
     * 判断当前BeanDefinition是否是单例的？
     *
     * @return 如果是单例的，return true；否则return false
     */
    override fun isSingleton() = scope == DEFAULT_SCOPE || scope == SCOPE_SINGLETON

    /**
     * 判断当前BeanDefinition是否是原型Bean？
     *
     * @return 如果是原型的return true，如果不是原型的，return false
     */
    override fun isPrototype() = scope == SCOPE_PROTOTYPE

    /**
     * 判断当前BeanDefinition是否是一个PrimaryBean？
     *
     * @return 如果它是primary的，return true，否则return false
     */
    override fun isPrimary() = this.primary

    /**
     * 实在它是否为Primary的？
     *
     * @param primary primary
     */
    override fun setPrimary(primary: Boolean) {
        this.primary = primary
    }

    /**
     * 获取该BeanDefinition要去进行自动注入的模式，BY_TYPE/BY_NAME ...
     */
    open fun getAutowireMode(): Int = this.autowireMode

    /**
     * 设置该BeanDefinition的自动注入模式
     */
    open fun setAutowireMode(mode: Int) {
        this.autowireMode = mode
    }

    override fun setFactoryMethodName(@Nullable factoryMethodName: String?) {
        this.factoryMethodName = factoryMethodName
    }

    /**
     * 获取一个BeanDefinition的FactoryMethod的name(@Bean方法的name)
     */
    @Nullable
    override fun getFactoryMethodName() = this.factoryMethodName

    override fun setFactoryBeanName(@Nullable factoryBeanName: String?) {
        this.factoryBeanName = factoryBeanName
    }

    /**
     * 获取当前BeanDefinition的factoryBean的name
     */
    @Nullable
    override fun getFactoryBeanName() = this.factoryBeanName

    /**
     * 获取当前的BeanDefinition的Bean的初始化方法
     *
     * @return initMethodName
     */
    @Nullable
    override fun getInitMethodName() = initMethodName

    override fun setInitMethodName(@Nullable initMethodName: String?) {
        this.initMethodName = initMethodName
    }

    /**
     * 设置当前BeanDefinition的destroyMethodName
     *
     * @param destroyMethodName 你想要设置的destroyMethodName
     */
    override fun setDestroyMethodName(@Nullable destroyMethodName: String?) {
        this.destroyMethodName = destroyMethodName
    }

    /**
     * 获取当前BeanDefinition的destroyMethodName
     *
     * @return destroyMethodName
     */
    @Nullable
    override fun getDestroyMethodName() = this.destroyMethodName

    /**
     * 设置当前BeanDefinition的scopeName
     *
     * @param scopeName scopeName
     */
    override fun setScope(scopeName: String) {
        this.scope = scopeName
    }

    /**
     * 设置当前BeanDefinition所属的作用域
     *
     * @return scopeName
     */
    override fun getScope() = this.scope

    /**
     * 设置当前BeanDefinition的role
     *
     * @param role role
     */
    override fun setRole(role: Int) {
        this.role = role
    }

    /**
     * 获取当前BeanDefinition的角色
     *
     * @return roleInt
     */
    override fun getRole() = this.role

    /**
     * 获取当前BeanDefinition是否是Autowire的候选Bean
     * 如果不是一个Autowire候选Bean，在进行Autowire时，不考虑它去进行注入
     *
     * @return 如果是Autowire候选Bean，return true；否则return false
     */
    override fun isAutowireCandidate() = this.autowireCandidate

    /**
     * 设置当前BeanDefinition是否是Autowire的候选Bean
     *
     * @param candidate 是否是autowire的候选Bean？true/false
     */
    override fun setAutowireCandidate(candidate: Boolean) {
        this.autowireCandidate = candidate
    }

    /**
     * 设置该BeanDefinition是否是抽象的？
     *
     * @param abstractFlag 是否是抽象的？
     */
    override fun setAbstract(abstractFlag: Boolean) {
        this.abstractFlag = abstractFlag
    }

    /**
     * 判断该BeanDefinition是否是抽象的？
     *
     * @return 如果beanClass是抽象的，return true；否则return false
     */
    override fun isAbstract() = abstractFlag

    /**
     * 设置当前BeanDefinition的实例化Supplier
     *
     * @param supplier 你想使用的实例化Supplier
     */
    override fun setInstanceSupplier(@Nullable supplier: Supplier<*>?) {
        this.instanceSupplier = supplier
    }

    /**
     * 获取该BeanDefinition的实例化Supplier，如果Supplier不为空，那么将会使用给定的Supplier去完成实例化，
     * 就不使用Spring的创建Bean的方式对它去进行实例化了，直接使用你给定的实例化策略去进行实例化
     *
     * @return 实例化Supplier(可能为null)
     */
    @Nullable
    override fun getInstanceSupplier() = this.instanceSupplier

    /**
     * 获取该BeanDefinition当中所有的需要去进行运行时方法重写的方法
     *
     * @return 需要去进行运行时重写的方法列表
     */
    override fun getMethodOverrides() = this.methodOverrides

    /**
     * 当前的BeanDefinition当中，是否有需要去进行运行时进行重写的方法？
     * 如果有需要去进行运行时重写的方法，那么会使用CGLIB完成代理的方式去进行重写
     *
     * @return 如果有运行时进行重写的方法，return true；否则return false
     */
    override fun hasMethodOverrides() = methodOverrides.getMethodOverrides().isNotEmpty()

    /**
     * 获取当前的BeanDefinition当中的PropertyValue列表，返回的是origin对象，可以进行增删改操作
     *
     * @return PropertyValue列表(MutablePropertyValues)
     */
    override fun getPropertyValues(): MutablePropertyValues = this.propertyValues

    /**
     * 判断当前BeanDefinition当中是否有PropertyValue？
     *
     * @return 如果已经添加了PropertyValue，那么return true；否则return false
     */
    override fun hasPropertyValues() = propertyValues.getPropertyValues().isNotEmpty()

    /**
     * 获取构造器参数列表
     *
     * @return 构造器参数列表
     */
    override fun getConstructorArgumentValues() = this.constructorArgumentValues

    /**
     * 是否存在有构造器参数？
     *
     * @return 如果存在有，return true；否则return false
     */
    override fun hasConstructorArgumentValues(): Boolean = !constructorArgumentValues.isEmpty()

    /**
     * 是否是懒加载的？如果是非懒加载的，那么在SpringBeanFactory启动时，就会完成实例化和初始化工作；
     * 如果设置为懒加载，那么只有在你第一次去进行getBean时，才会完成该Bean的初始化工作
     */
    override fun isLazyInit() = lazyInit

    override fun setLazyInit(lazyInit: Boolean) {
        this.lazyInit = lazyInit
    }

    override fun setDependsOn(dependsOn: Array<String>) {
        this.dependsOn = dependsOn
    }

    /**
     * 在创建当前Bean时，需要依赖哪些Bean才能去完成创建，在对当前的Bean去进行实例化之前，
     * SpringBeanFactory需要先去对所有依赖的Bean去进行实例化，再完成当前的Bean的实例化
     *
     * @return dependsOn的beanName列表
     */
    override fun getDependsOn() = dependsOn

    /**
     * 获取当前BeanDefinition当中的beanClass，有可能为null
     *
     * @return beanClass
     */
    @Nullable
    override fun getBeanClass() = beanClass

    /**
     * 设置beanClassName
     *
     * @param beanClassName beanClassName
     */
    open fun setBeanClassName(beanClassName: String?) {
        this.beanClassName = beanClassName
    }

    /**
     * 获取beanClassName，有可能为null
     *
     * @return beanClassName
     */
    @Nullable
    override fun getBeanClassName() = beanClass?.name ?: beanClassName

    /**
     * 设置当前BeanDefinition的beanClass
     *
     * @param beanClass beanClass
     */
    override fun setBeanClass(@Nullable beanClass: Class<*>?) {
        this.beanClass = beanClass
    }

    /**
     * 判断是否有beanClass
     *
     * @return 如果当前BeanDefinition当中已经有beanClass了，return true；否则return false
     */
    open fun hasBeanClass(): Boolean = beanClass != null

    /**
     * 该BeanDefinition是否是一个合成的BeanDefinition? 如果为true, 代表它不会经过AOP的自动代理
     *
     * @param synthetic 是否是合成的标志位
     */
    open fun setSynthetic(synthetic: Boolean) {
        this.synthetic = synthetic
    }

    /**
     * 该BeanDefinition是否是一个合成的BeanDefinition? 如果为true, 代表它不会经过AOP的自动代理
     *
     * @return synthetic
     */
    open fun isSynthetic(): Boolean = this.synthetic

    /**
     * 设置当前BeanDefinition的Resource
     *
     * @param resource Resource
     */
    open fun setResource(resource: Resource?) {
        this.resource = resource
    }

    /**
     * 获取当前BeanDefinition的Resource
     *
     * @return Resource
     */
    open fun getResource(): Resource? = this.resource

    /**
     * 设置BeanDefinition的description
     *
     * @param description description
     */
    open fun setDescription(@Nullable description: String?) {
        this.description = description
    }

    /**
     * 获取BeanDefinition的Description
     *
     * @return description
     */
    @Nullable
    open fun getDescription(): String? = this.description

    /**
     * 当前BeanDefinition的资源的描述信息
     *
     * @return 资源描述信息(如果不存在的话，那么return null)
     */
    @Nullable
    override fun getResourceDescription(): String? = resource?.getDescription()
}