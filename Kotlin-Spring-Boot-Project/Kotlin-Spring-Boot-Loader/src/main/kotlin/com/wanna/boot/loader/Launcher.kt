package com.wanna.boot.loader

/**
 * 用于启动整个应用的Launcher
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 */
abstract class Launcher {

    /**
     * 供子类当中去创建main方法，并去完成进行调用
     *
     * @param args 启动应用时需要用到的方法参数列表
     */
    protected open fun launch(args: Array<String>) {

    }

    /**
     * 获取主类
     *
     * @return 主类
     */
    abstract fun getMainClass(): String

}