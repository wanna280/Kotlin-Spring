package com.wanna.framework.core.io

import com.wanna.framework.lang.Nullable
import java.io.FileNotFoundException

/**
 * 用于提供描述信息的的Resource
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/30
 *
 * @param description 描述信息
 */
open class DescriptiveResource(@Nullable private val description: String?) : AbstractResource() {

    /**
     * 我们只是用来去进行描述性的资源对象，不可读
     *
     * @return 不会return, 一定丢出来异常
     * @throws FileNotFoundException 当前资源不可读
     */
    @Throws(FileNotFoundException::class)
    override fun getInputStream() = throw FileNotFoundException("对于[$description]只是一个描述的Resource, 无法进行读取")

    /**
     * 我们只是用来去进行描述性的资源对象，不可读
     *
     * @return false
     */
    override fun isReadable() = false

    /**
     * 我们只是用来去进行描述性的资源对象，并不真实存在
     *
     * @return false
     */
    override fun exists() = false

    /**
     * 获取当前资源的描述信息
     *
     * @return 描述信息(可以为null)
     */
    override fun getDescription(): String? = this.description
}