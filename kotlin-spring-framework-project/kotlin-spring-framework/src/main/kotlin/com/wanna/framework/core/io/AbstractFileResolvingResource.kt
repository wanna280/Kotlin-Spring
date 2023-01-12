package com.wanna.framework.core.io

import com.wanna.framework.util.ResourceUtils
import java.io.File
import java.io.IOException

/**
 * 为Resource提供对于文件的解析功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 */
abstract class AbstractFileResolvingResource : AbstractResource() {

    /**
     * 是否是文件? 通过url当中去检查协议是否是"file"的方式去进行检查
     *
     * @return 如果是文件的话, return true; 否则return false
     */
    override fun isFile(): Boolean {
        try {
            val url = getURL()
            return url.protocol == ResourceUtils.URL_PROTOCOL_FILE
        } catch (ex: IOException) {
            return false
        }
    }

    /**
     * 获取资源的文件对象
     *
     * @return File
     */
    override fun getFile(): File = File(getURL().toString())
}