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
    fun getKeyTenant(dataId: String, group: String, tenant: String): String {
        return tenant + "_" + group + "_" + dataId
    }
}