package com.wanna.boot.loader.tools

/**
 * 归档文件的布局方式
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
interface Layout {

    /**
     * 获取当前布局方式的LauncherClassName, 负责引导SpringBoot的启动
     *
     * @return Launcher
     */
    fun getLauncherClassName(): String

    fun getClassesLocation(): String
}