package com.wanna.framework.simple.test.annotation

import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.annotation.MergedAnnotations
import com.wanna.framework.util.ReflectionUtils

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/13
 */
class AnnotationScannerTest {
}

class User

@Component("userService1")
interface UserService {
    @Bean("user1")
    fun getUser(): User
}

@Component("userService2")
abstract class BaseUserService : UserService {

    @Bean("user2")
    override fun getUser() = User()
}

class UserServiceImpl : BaseUserService(), UserService {
    override fun getUser(): User = User()
}


fun main() {
    val method = ReflectionUtils.findMethod(UserServiceImpl::class.java, "getUser")!!

    val interfaceAnn =
        MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(Bean::class.java)
    val superClassAnn =
        MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.SUPERCLASS).get(Bean::class.java)
    val directAnn = MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.DIRECT).get(Bean::class.java)

    // 预期获取到接口上的...
    assert(interfaceAnn.getString("value") == "user1")

    // 预期获取到父类上的
    assert(superClassAnn.getString("value") == "user2")

    // 预期获取不到
    assert(!directAnn.present)


    val superClassComponent =
        MergedAnnotations.from(UserServiceImpl::class.java, MergedAnnotations.SearchStrategy.SUPERCLASS)
            .get(Component::class.java)

    val interfaceComponent =
        MergedAnnotations.from(UserServiceImpl::class.java, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
            .get(Component::class.java)

    val directComponent = MergedAnnotations.from(UserServiceImpl::class.java, MergedAnnotations.SearchStrategy.DIRECT)
        .get(Component::class.java)


    // 预期获取到接口上的
    assert(interfaceComponent.getString("value") == "userService1")

    // 预期获取到父类上的
    assert(superClassComponent.getString("value") == "userService2")

    // 预期直接获取获取不到
    assert(!directComponent.present)


}