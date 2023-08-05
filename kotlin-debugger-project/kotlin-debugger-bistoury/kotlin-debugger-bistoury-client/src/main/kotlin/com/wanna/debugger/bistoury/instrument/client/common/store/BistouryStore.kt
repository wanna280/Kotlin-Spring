package com.wanna.debugger.bistoury.instrument.client.common.store

import java.io.File
import java.nio.file.Paths

/**
 * Bistoury的文件存放位置Store
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/4
 */
object BistouryStore {

    /**
     * 用于获取Bistoury的文件存放位置的系统属性Key
     */
    private const val DEFAULT_BISTOURY_STORE_PATH_CONFIG_KEY = "bistoury.store.path"

    /**
     * Bistoury的默认文件Store存储位置
     */
    private const val DEFAULT_BISTOURY_STORE_PATH = "/tmp/bistoury/store"

    /**
     * Bistoury的文件存放位置
     */
    @JvmStatic
    private val STORE_PATH: String

    init {
        File(DEFAULT_BISTOURY_STORE_PATH).mkdirs()
        val path = System.getProperty(DEFAULT_BISTOURY_STORE_PATH_CONFIG_KEY, DEFAULT_BISTOURY_STORE_PATH)
        this.STORE_PATH = path
    }

    /**
     * 获取Bistoury文件存放的根路径
     *
     * @return rootStorePath
     */
    @JvmStatic
    fun getRootStorePath(): String = this.STORE_PATH

    /**
     * 获取给定的子目录的文件存放位置
     *
     * @param child child directory
     */
    @JvmStatic
    fun getStorePath(child: String): String {
        return Paths.get(STORE_PATH).resolve(child).toString()
    }

}