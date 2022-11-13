package com.wanna.nacos.config.server.utils

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.nacos.config.server.service.ConfigCacheService
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

        // 检查客户端传递过来的所有的clientMd5, 看是否是最新的?如果不是最新的, 那么需要收集到changedGroupKeys当中, 返回给客户端
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
            builder.append(key[0]).append(Char(2))
            builder.append(key[1])
            if (key.size == 3 && key[2].isNotBlank()) {
                builder.append(Char(2)).append(key[2])
            }
            builder.append("\n")
        }
        return URLEncoder.encode(builder.toString(), "UTF-8")
    }
}