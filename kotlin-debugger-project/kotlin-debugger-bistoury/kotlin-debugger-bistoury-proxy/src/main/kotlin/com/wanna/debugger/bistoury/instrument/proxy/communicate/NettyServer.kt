package com.wanna.debugger.bistoury.instrument.proxy.communicate

/**
 * NettyServer
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/6/22
 */
interface NettyServer {

    /**
     * 启动NettyServer
     */
    fun start()

    /**
     * 关闭NettyServer
     */
    fun stop()
}