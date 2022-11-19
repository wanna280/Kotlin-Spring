package com.wanna.nacos.client.utils

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
object ParamUtils {

    @JvmStatic
    private var perTaskConfigSize = 3000

    /**
     * 获取每个任务需要处理的ConfigFile的数量(默认值为3000)
     *
     * @return 每个LongPollingRunnable任务需要处理的配置文件的数量
     */
    @JvmStatic
    fun getPerTaskConfigSize(): Int = perTaskConfigSize
}