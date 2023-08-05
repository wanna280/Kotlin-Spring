package com.wanna.framework.beans

/**
 * 这是一个在初始化完容器中所有的单例Bean之后的才会执行的回调, 也可以用来完成Bean的初始化工作
 */
fun interface SmartInitializingSingleton {
    fun afterSingletonsInstantiated()
}