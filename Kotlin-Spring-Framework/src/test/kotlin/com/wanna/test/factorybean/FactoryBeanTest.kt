package com.wanna.test.factorybean

import com.wanna.framework.beans.factory.FactoryBean
import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Import
import com.wanna.framework.context.stereotype.Component

@Import([UserFactoryBean::class])
@Configuration
class FactoryBeanTest

class User

@Component
class UserFactoryBean : FactoryBean<User> {
    override fun getObjectType() = User::class.java
    override fun getObject() = User()
    override fun isSingleton() = true
    override fun isPrototype() = false
}

fun main() {
    val applicationContext = AnnotationConfigApplicationContext(FactoryBeanTest::class.java)
    val bean = applicationContext.getBean("&com.wanna.test.factorybean.UserFactoryBean")
    println(bean)
    val beanObject = applicationContext.getBean("com.wanna.test.factorybean.UserFactoryBean")
    println(beanObject)
}