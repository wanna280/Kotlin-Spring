package com.wanna.nacos.client.config.impl

/**
 * Key的生成器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
object GroupKey {

    @JvmStatic
    fun getKey(dataId: String, group: String): String {
        return getKeyTenant(dataId, group, "")
    }

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
        return groupKey.split("_").toTypedArray()
    }
}