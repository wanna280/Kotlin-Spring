package com.wanna.boot.devtools.restart.server

import com.wanna.boot.devtools.restart.Restarter
import com.wanna.boot.devtools.restart.classloader.ClassLoaderFiles

open class StartServer {

    open fun updateAndRestart(classLoaderFiles: ClassLoaderFiles) {
        restart(classLoaderFiles)
    }

    /**
     * 使用Restarter去完成当前Application的重启
     *
     * @param classLoaderFiles ClassLoaderFiles
     */
    protected open fun restart(classLoaderFiles: ClassLoaderFiles) {
        Restarter.getInstance()!!.addClassLoaderFiles(classLoaderFiles)
        Restarter.getInstance()!!.restart()
    }
}