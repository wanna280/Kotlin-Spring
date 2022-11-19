package com.wanna.nacos.config.server.utils

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.nacos.api.common.Constants
import com.wanna.nacos.config.server.service.ConfigCacheService
import com.wanna.nacos.config.server.utils.GroupKey2.getKey
import com.wanna.nacos.config.server.utils.GroupKey2.getKeyTenant
import java.net.URLEncoder

/**
 * Md5的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
object MD5Utils {

    /**
     * 根据给定的probeModify信息, 去解码成为clientMd5Map
     *
     * @param probeModify 客户端发送的探测可能发生变化的配置文件MD5信息
     * @return 将probeModify字符串转换成为groupKey和Md5的Map(Key-groupKey, Value-MD5)
     */
    @JvmStatic
    fun getClientMd5Map(probeModify: String): Map<String, String> {
        val clientMd5Map = LinkedHashMap<String, String>()
        val clientMd5List = probeModify.split(Constants.LINE_SEPARATOR)
        clientMd5List.forEach {
            if (it.isBlank()) {
                return@forEach
            }
            val clientMd5Info = it.split(Constants.WORD_SEPARATOR)
            if (clientMd5Info.size < 3 || clientMd5Info.size > 4) {
                throw IllegalArgumentException("probeModify格式不对")
            }
            // 如果长度为3, 那么分别是dataId/group/md5;
            // 如果长度为4, 那么分别为dataId/group/md5/tenant
            if (clientMd5Info.size == 3) {
                clientMd5Map[getKey(clientMd5Info[0], clientMd5Info[1])] = clientMd5Info[2]
            } else {
                clientMd5Map[getKeyTenant(clientMd5Info[0], clientMd5Info[1], clientMd5Info[3])] =
                    clientMd5Info[2]
            }
        }
        return clientMd5Map
    }

    /**
     * 比较MD5是否是最新的
     *
     * @param request request
     * @param response response
     * @param clientMd5Map clientMd5Map(key-groupKey, Value-clientMd5)
     * @return clientMd5Map和ConfigCache的比较结果当中, 存在哪些有变化的groupKey?
     */
    @JvmStatic
    fun compareMd5(
        request: HttpServerRequest,
        @Nullable response: HttpServerResponse?,
        clientMd5Map: Map<String, String>
    ): List<String> {
        val changedGroupKeys = ArrayList<String>()

        // 检查客户端传递过来的clientMd5Map当中的所有的md5值, 看是否是最新的?
        // 如果不是最新的, 那么需要收集到changedGroupKeys当中, 返回给客户端
        clientMd5Map.forEach { (groupKey, clientMd5) ->
            if (!ConfigCacheService.isUptodate(groupKey, clientMd5, RequestUtils.getRemoteIp(request), "")) {
                changedGroupKeys += groupKey
            }
        }
        return changedGroupKeys
    }

    /**
     * 比较Md5的结果字符串, 将给定的这些groupKey, 全部去拆分成为"dataId"/"group"/"tenant"三个部分
     *
     * @param changedGroupKeys 发生变化的GroupKeys
     * @return 生成的"dataId"/"group"/"tenant"三个部分的Key的列表, 并使用URLEncoder去进行编码
     */
    @JvmStatic
    fun compareMd5ResultString(@Nullable changedGroupKeys: List<String>?): String {
        changedGroupKeys ?: return ""
        val builder = StringBuilder()
        changedGroupKeys.forEach {
            val key = GroupKey2.parseKey(it)
            builder.append(key[0]).append(Constants.WORD_SEPARATOR)
            builder.append(key[1])
            if (key.size == 3 && key[2].isNotBlank()) {
                builder.append(Constants.WORD_SEPARATOR).append(key[2])
            }
            builder.append(Constants.LINE_SEPARATOR)
        }
        return URLEncoder.encode(builder.toString(), Constants.ENCODE)
    }
}