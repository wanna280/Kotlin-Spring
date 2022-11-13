package com.wanna.nacos.config.server.service

/**
 * ConfigCacheService
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
class ConfigCacheService {

    companion object {

        /**
         * 检查给定groupKey对应的文件的Md5是否已经是最新的了?
         *
         * @param groupKey groupKey(dataId&group/tenant)
         * @param clientMd5 当前客户端的MD5值
         * @param ip 客户端IP
         * @param tag tag
         */
        @JvmStatic
        fun isUptodate(groupKey: String, clientMd5: String, ip: String, tag: String): Boolean {
            return false
        }
    }

}