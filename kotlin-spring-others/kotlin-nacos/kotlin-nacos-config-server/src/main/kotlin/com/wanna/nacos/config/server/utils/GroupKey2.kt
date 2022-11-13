package com.wanna.nacos.config.server.utils

/**
 * Key的生成器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
object GroupKey2 {

    @JvmStatic
    fun getKeyTenant(dataId: String, group: String, tenant: String): String {
        return tenant + "_" + group + "_" + dataId
    }

    /**
     * 将一个GroupKey去拆分转换成为"dataId"、"group"、"tenant"三个部分
     *
     * @param groupKey groupKey
     * @return "dataId"/"group"/"tenant"
     */
    @JvmStatic
    fun parseKey(groupKey: String): Array<String> {
        return arrayOf("", "", "")
    }
}