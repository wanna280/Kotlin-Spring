package com.wanna.boot.devtools.restart.server

import com.wanna.boot.devtools.restart.Restarter
import com.wanna.boot.devtools.restart.classloader.ClassLoaderFiles

open class StartServer {

    /**
     * 更新并重启
     *
     * @param classLoaderFiles 远程传输过来的文件信息
     */
    open fun updateAndRestart(classLoaderFiles: ClassLoaderFiles) {
        restart(classLoaderFiles)
    }

    /**
     * 使用Restarter去完成当前Application的重启
     *
     * @param classLoaderFiles ClassLoaderFiles
     */
    protected open fun restart(classLoaderFiles: ClassLoaderFiles) {
        val restarter = Restarter.getInstance()!!

        // 添加远程传递过来的ClassLoaderFiles
        restarter.addClassLoaderFiles(classLoaderFiles)

        // 执行restart
        restarter.restart()
    }
}