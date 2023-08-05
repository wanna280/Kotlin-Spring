package com.wanna.boot.autoconfig

import com.wanna.boot.SpringApplication
import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingClass
import com.wanna.boot.context.properties.ConfigurationProperties
import com.wanna.boot.context.properties.ConstructorBinding
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.EnableAspectJWeaving
import com.wanna.framework.context.annotation.LoadTimeWeavingConfigurer
import com.wanna.framework.instrument.classloading.InstrumentationLoadTimeWeaver
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import java.util.concurrent.TimeUnit

@EnableAspectJWeaving
@ConditionalOnMissingClass(value = ["com.wanna.boot.autoconfigure.MyReactiveWebServerFactory1"])
@SpringBootApplication
@EnableConfigurationProperties([ConfigurationPropertiesConstructorBinding::class])
open class ConditionTest {

    @Bean
    fun loadTimeWeavingConfigurer(): LoadTimeWeavingConfigurer {
        return LoadTimeWeavingConfigurer { InstrumentationLoadTimeWeaver(MyClassLoader.INSTANCE) }
    }
}

@ConfigurationProperties
class ConfigurationPropertiesConstructorBinding() {

    @ConstructorBinding
    constructor(name: String) : this()
}

class MyClassLoader : ClassLoader() {
    override fun loadClass(name: String?): Class<*> {
        if (name == null) {
            throw IllegalStateException("")
        }
        val stream = getSystemClassLoader().getResourceAsStream(name.replace(".", "/") + ".class")
        if (name.startsWith("com.wanna")) {
            val readAllBytes = stream.readAllBytes()
            return defineClass(name, readAllBytes, 0, readAllBytes.size)
        }
        return super.loadClass(name)
    }

    companion object {
        val INSTANCE = MyClassLoader()
    }
}

@Aspect
open class UserAspect {
    @Pointcut("execution(* com.wanna.boot.autoconfigure.UserService.*(..))")
    fun pointcut() {
    }

    @Before("pointcut()")
    fun before() {
        println("-------before----------")
    }

    @After("pointcut()")
    fun after() {
        println("------after------")
    }

    companion object {
        @JvmStatic
        fun aspectOf(): UserAspect {
            return UserAspect()
        }
    }
}


class UserService {
    fun sayUser() {
        println("HelloWorld")
    }
}

fun main(vararg args: String) {
    val applicationContext = SpringApplication.run(ConditionTest::class.java)
    TimeUnit.MILLISECONDS.sleep(1000)

    val clazz = MyClassLoader.INSTANCE.loadClass("com.wanna.boot.autoconfigure.UserService")
    val instance = clazz.getDeclaredConstructor().newInstance()
    val method = clazz.getMethod("sayUser")
    method.invoke(instance)


}