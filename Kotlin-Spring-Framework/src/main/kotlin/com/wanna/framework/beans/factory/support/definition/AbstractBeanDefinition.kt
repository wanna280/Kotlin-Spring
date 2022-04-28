package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.beans.method.ConstructorArgumentValues
import com.wanna.framework.beans.method.MutablePropertyValues
import com.wanna.framework.beans.factory.support.definition.BeanDefinition.Companion.ROLE_APPLICATION
import com.wanna.framework.beans.factory.support.definition.BeanDefinition.Companion.SCOPE_PRTOTYPE
import com.wanna.framework.beans.factory.support.definition.BeanDefinition.Companion.SCOPE_SINGLETON
import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataAttributeAccessor
import com.wanna.framework.beans.method.MethodOverrides
import com.wanna.framework.context.AutowireCapableBeanFactory
import java.util.function.Supplier

/**
 * 这是一个抽象的BeanDefinition，它继承了BeanMetadataAttributeAccessor，支持进行属性的设置和获取
 */
abstract class AbstractBeanDefinition constructor(_beanClass: Class<*>?) : BeanDefinition,
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
     */
    open fun <T : BeanDefinition> copy(origin: BeanDefinition?, target: T) {
        if (origin == null) {
            return
        }
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
        }
    }

    private var beanClass: Class<*>? = _beanClass

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

    override fun isSingleton(): Boolean {
        return scope == DEFAULT_SCOPE || scope == SCOPE_SINGLETON
    }

    override fun isPrototype(): Boolean {
        return scope == SCOPE_PRTOTYPE
    }

    override fun isPrimary(): Boolean {
        return primary
    }

    override fun setPrimary(primary: Boolean) {
        this.primary = primary
    }

    fun getAutowireMode(): Int {
        return autowireMode
    }

    fun setAutowireMode(mode: Int) {
        this.autowireMode = mode
    }

    override fun setFactoryMethodName(factoryMethodName: String?) {
        this.factoryMethodName = factoryMethodName
    }

    override fun getFactoryMethodName(): String? {
        return factoryMethodName
    }

    override fun setFactoryBeanName(factoryBeanName: String?) {
        this.factoryBeanName = factoryBeanName
    }

    override fun getFactoryBeanName(): String? {
        return factoryBeanName
    }

    override fun getInitMethodName(): String? {
        return initMethodName
    }

    override fun setInitMethodName(initMethodName: String?) {
        this.initMethodName = initMethodName
    }

    override fun setDestoryMethodName(destoryMethodName: String?) {
        this.destoryMethodName = destoryMethodName
    }

    override fun getDestoryMethodName(): String? {
        return destoryMethodName
    }

    override fun setScope(scopeName: String) {
        this.scope = scopeName
    }

    override fun getScope(): String {
        return scope
    }

    override fun setRole(role: Int) {
        this.role = role
    }

    override fun getRole(): Int {
        return role
    }

    override fun isAutowireCandidate(): Boolean {
        return autowireCandidate
    }

    override fun setAutowireCandidate(candidate: Boolean) {
        this.autowireCandidate = candidate
    }

    override fun setAbstract(abstractFlag: Boolean) {
        this.abstractFlag = abstractFlag
    }

    override fun isAbstract(): Boolean {
        return abstractFlag
    }

    override fun setInstanceSupplier(supplier: Supplier<*>?) {
        this.instanceSupplier = supplier
    }

    override fun getInstanceSupplier(): Supplier<*>? {
        return instanceSupplier
    }

    override fun getMethodOverrides(): MethodOverrides {
        return methodOverrides
    }

    override fun hasMethodOverrides(): Boolean {
        return methodOverrides.getMethodOverrides().isNotEmpty()
    }

    override fun getPropertyValues(): MutablePropertyValues {
        return propertyValues
    }

    override fun hasPropertyValues(): Boolean {
        return propertyValues.getPropertyValues().isNotEmpty()
    }

    override fun getConstructorArgumentValues(): ConstructorArgumentValues {
        return constructorArgumentValues
    }

    override fun hasConstructorArgumentValues(): Boolean {
        return false
    }

    override fun isLazyInit(): Boolean {
        return lazyInit
    }

    override fun setLazyInit(lazyInit: Boolean) {
        this.lazyInit = lazyInit
    }

    override fun setDependsOn(dependsOn: Array<String>) {
        this.dependsOn = dependsOn
    }

    override fun getDependsOn(): Array<String> {
        return dependsOn
    }

    override fun getBeanClass(): Class<*>? {
        return beanClass
    }

    override fun setBeanClass(beanClass: Class<*>?) {
        this.beanClass = beanClass
    }
}