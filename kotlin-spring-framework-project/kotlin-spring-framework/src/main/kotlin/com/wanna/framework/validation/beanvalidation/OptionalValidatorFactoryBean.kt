package com.wanna.framework.validation.beanvalidation

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 对于一个可选的Validator的实现的FactoryBean；
 * 对于[LocalValidatorFactoryBean]来说，会在初始化时，去完成Validator的初始化工作，
 * 但是当初始化失败时，就会出现异常，因此我们在这个类当中要做的事情，就是如果父类找不到
 * 合适的Validator的话，那么我们就忽略掉即可，因为毕竟就是因为没有找到所以出现的问题
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/26
 */
open class OptionalValidatorFactoryBean : LocalValidatorFactoryBean() {

    /**
     * Logger
     */
    protected val logger: Logger = LoggerFactory.getLogger(OptionalValidatorFactoryBean::class.java)

    /**
     * 重写父类的寻找Validator的逻辑，我们直接去吃掉异常即可
     */
    override fun afterPropertiesSet() {
        try {
            super.afterPropertiesSet()
        } catch (ex: Exception) {
            logger.debug("安装Bean Validation Provider失败", ex)
        }
    }
}