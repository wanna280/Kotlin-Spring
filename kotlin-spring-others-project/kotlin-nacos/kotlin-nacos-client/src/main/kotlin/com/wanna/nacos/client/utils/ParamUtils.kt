package com.wanna.nacos.client.utils

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
object ParamUtils {

    /**
     * 每个任务需要负责去进行处理的配置文件的数量
     */
    @JvmStatic
    private var perTaskConfigSize = 3000

    /**
     * serverPort
     */
    @JvmStatic
    private var serverPort = 9966

    /**
     * 获取默认的ServerPort
     */
    @JvmStatic
    fun getDefaultServerPort(): Int = serverPort

    /**
     * 获取每个任务需要处理的ConfigFile的数量(默认值为3000)
     *
     * @return 每个LongPollingRunnable任务需要处理的配置文件的数量
     */
    @JvmStatic
    fun getPerTaskConfigSize(): Int = perTaskConfigSize
}