package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.beans.factory.config.ConstructorArgumentValues
import com.wanna.framework.beans.MutablePropertyValues
import com.wanna.framework.beans.factory.support.definition.BeanDefinition.Companion.ROLE_APPLICATION
import com.wanna.framework.beans.factory.support.definition.BeanDefinition.Companion.SCOPE_PRTOTYPE
import com.wanna.framework.beans.factory.support.definition.BeanDefinition.Companion.SCOPE_SINGLETON
import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataAttributeAccessor
import com.wanna.framework.beans.method.MethodOverrides
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import java.util.function.Supplier

/**
 * 这是一个抽象的BeanDefinition，它继承了BeanMetadataAttributeAccessor，支持进行属性的设置和获取
 */
abstract class AbstractBeanDefinition constructor(private var beanClass: Class<*>? = null) : BeanDefinition,
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
    open fun <T : BeanDefinition> copy(origin: BeanDefinition?, target: T) {
        origin ?: return
        target.setBeanClass(origin.getBeanClass())
        target.setAbstract(origin.isAbstract())
        target.setPrimary(origin.isPrimary())
        target.setInitMethodName(origin.getInitMethodName())
        target.setDestoryMethodName(origin.getDestoryMethodName())
        target.setScope(origin.getScope())
        target.setRole(origin.getRole())
        target.setInstanceSupplier(origin.getInstanceSupplier())
        target.setFactoryBeanName(origin.getFactoryBeanName())
        target.setFactoryMethodName(origin.getFactoryMethodName())
        target.setLazyInit(origin.isLazyInit())
        target.setAutowireCandidate(origin.isAutowireCandidate())
        if (origin is AbstractBeanDefinition && target is AbstractBeanDefinition) {
            target.setSource(origin.getSource())
            target.setAutowireMode(origin.getAutowireMode())
            target.dependsOn = origin.getDependsOn()
            target.methodOverrides = MethodOverrides(origin.getMethodOverrides())
            target.propertyValues = MutablePropertyValues(MutablePropertyValues())
            target.constructorArgumentValues = ConstructorArgumentValues(origin.getConstructorArgumentValues())
            this.getPropertyValues()
                .addPropertyValues(origin.getPropertyValues().getPropertyValues().toList())
        }
    }

    private var primary: Boolean = false  // 在进行autowire时，它是否是优先注入的Bean？
    private var initMethodName: String? = null  // 初始化方法的name
    private var destoryMethodName: String? = null // destory的方法name

    private var scope: String = DEFAULT_SCOPE  // bean的作用域，比如singleton/prototype
    private var role: Int = ROLE_APPLICATION   // bean的角色，Application(0)、Support(1)和Infrastructure(2)

    private var abstractFlag: Boolean = false  // 它是否是抽象的？

    private var instanceSupplier: Supplier<*>? = null  // 实例化Bean的Supplier

    private var factoryMethodName: String? = null  // @Bean的方法name
    private var factoryBeanName: String? = null  // @Bean的方法所在类的Bean的beanName

    private var autowireMode: Int = AUTOWIRE_NO  // autowire的模式，BY_TYPE/BY_NAME/CONSTRUCTOR
    private var autowireCandidate: Boolean = true  // 是否是一个候选去进行autowire的Bean

    private var lazyInit: Boolean = false  // 是否懒加载，默认为false
    private var dependsOn: Array<String> = emptyArray()  // Bean所依赖的Bean的列表

    private var methodOverrides: MethodOverrides = MethodOverrides()  // 运行时方法重写的列表
    private var propertyValues: MutablePropertyValues = MutablePropertyValues()  // 要对Bean进行设置的属性值列表
    private var constructorArgumentValues: ConstructorArgumentValues = ConstructorArgumentValues()  // Bean的构造器参数列表

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
    override fun isPrototype() = scope == SCOPE_PRTOTYPE

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
    open fun getAutowireMode() = this.autowireMode

    /**
     * 设置该BeanDefinition的自动注入模式
     */
    open fun setAutowireMode(mode: Int) {
        this.autowireMode = mode
    }

    override fun setFactoryMethodName(factoryMethodName: String?) {
        this.factoryMethodName = factoryMethodName
    }

    /**
     * 获取一个BeanDefinition的FactoryMethod的name(@Bean方法的name)
     */
    override fun getFactoryMethodName() = this.factoryMethodName

    override fun setFactoryBeanName(factoryBeanName: String?) {
        this.factoryBeanName = factoryBeanName
    }

    /**
     * 获取当前BeanDefinition的factoryBean的name
     */
    override fun getFactoryBeanName() = this.factoryBeanName

    /**
     * 获取当前的BeanDefinition的Bean的初始化方法
     *
     * @return initMethodName
     */
    override fun getInitMethodName() = initMethodName

    override fun setInitMethodName(initMethodName: String?) {
        this.initMethodName = initMethodName
    }

    /**
     * 设置当前BeanDefinition的destroyMethodName
     *
     * @param destoryMethodName 你想要设置的destroyMethodName
     */
    override fun setDestoryMethodName(destoryMethodName: String?) {
        this.destoryMethodName = destoryMethodName
    }

    /**
     * 获取当前BeanDefinition的destroyMethodName
     *
     * @return destroyMethodName
     */
    override fun getDestoryMethodName() = this.destoryMethodName

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
    override fun setInstanceSupplier(supplier: Supplier<*>?) {
        this.instanceSupplier = supplier
    }

    /**
     * 获取该BeanDefinition的实例化Supplier，如果Supplier不为空，那么将会使用给定的Supplier去完成实例化，
     * 就不使用Spring的创建Bean的方式对它去进行实例化了，直接使用你给定的实例化策略去进行实例化
     *
     * @return 实例化Supplier(可能为null)
     */
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

    override fun getConstructorArgumentValues(): ConstructorArgumentValues {
        return constructorArgumentValues
    }

    override fun hasConstructorArgumentValues(): Boolean {
        return false
    }

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
    override fun getBeanClass() = beanClass

    /**
     * 获取beanClassName，有可能为null
     *
     * @return beanClassName
     */
    override fun getBeanClassName() = beanClass?.name

    /**
     * 设置当前BeanDefinition的beanClass
     *
     * @param beanClass beanClass
     */
    override fun setBeanClass(beanClass: Class<*>?) {
        this.beanClass = beanClass
    }
}