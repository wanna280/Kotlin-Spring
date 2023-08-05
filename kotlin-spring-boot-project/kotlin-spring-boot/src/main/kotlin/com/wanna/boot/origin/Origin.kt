package com.wanna.boot.origin

import com.wanna.framework.lang.Nullable

/**
 * 代表了一个Item的唯一来源信息, 例如从文件当中去进行加载得到的内容, 就会有相关的文件名&文件的行号的描述信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/7
 *
 * @see OriginProvider
 */
interface Origin {

    /**
     * parent Origin(or null)
     */
    val parent: Origin?
        get() = null


    companion object {

        /**
         * 从一个给定的source当中去提取出来[Origin]
         *
         * @param source source
         * @return 提取得到的[Origin], 不存在的话, return null
         */
        @Nullable
        @JvmStatic
        fun from(source: Any): Origin? {
            if (source is Origin) {
                return source
            }
            if (source is OriginProvider) {
                return source.getOrigin()
            }
            if (source is Throwable) {
                return from(source)
            }
            return null
        }
    }
}