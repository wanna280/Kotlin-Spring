package com.wanna.framework.web.server

/**
 * 这是一个操作的回调函数(本身应该是定义在Tomcat上, 但是我们这里没有Tomcat, 于是自己封装一层)
 */
interface ActionHook {

    /**
     * 针对操作类型的Code, 去执行对应的回调函数
     *
     * @param code ActionCode
     * @param param 携带的附加
     */
    fun action(code: ActionCode, param: Any?)
}