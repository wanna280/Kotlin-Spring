package com.wanna.logger.api

/**
 * 这是顶层的API规范, 是LoggerFactory的一层抽象, 负责去提供获取和创建Logger的方式
 */
interface ILoggerFactory {

    /**
     * 通过loggerName去获取到Logger组件
     *
     * @param name loggerName
     * @return 获取到的Logger
     */
    fun getLogger(name: String): Logger

    /**
     * 通过clazz的全类名作为loggerName去获取到Logger; 在接口当中已经提供了默认实现, 子类也可以去进行自定义逻辑
     *
     * @param clazz 目标类的全类名
     * @return 获取到的的Logger
     */
    fun getLogger(clazz: Class<*>): Logger = getLogger(clazz.name)
}