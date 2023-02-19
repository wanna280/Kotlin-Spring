package com.wanna.boot.gradle.tasks.bundling

/**
 * ZIP归档文件当中的Entry, 是否是被压缩过的状态枚举值
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
enum class ZipCompression {
    /**
     * 状态为非压缩的状态
     */
    STORED,

    /**
     * 状态为压缩的状态
     */
    DEFLATED
}