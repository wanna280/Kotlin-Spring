package com.wanna.boot.devtools.filewatch

import com.wanna.boot.devtools.filewatch.ChangedFile.Type.*
import java.io.File
import java.io.FileFilter
import java.util.*
import javax.annotation.Nullable

/**
 * 在某一个时间点, 某个文件夹的Snapshot(快照)信息
 *
 * @param directory 要去进行描述的文件夹
 */
open class DirectorySnapshot(val directory: File) {
    companion object {
        /**
         * ("."/"..")文件夹的情况, 它不是个真正的文件夹
         */
        @JvmField
        val DOTS = setOf(".", "..")
    }

    /**
     * Date
     */
    val date = Date()

    /**
     * 该文件夹下的所有文件的快照信息
     */
    private var files: Set<FileSnapshot>

    init {
        if (directory.isFile) {
            throw IllegalStateException("给定的directory必须是一个文件夹, 而不是一个文件")
        }
        val files = LinkedHashSet<FileSnapshot>()
        // 收集该文件夹下的所有的文件的快照信息
        collectFiles(directory, files)
        this.files = files
    }

    /**
     * 获取给定的当前(snapshot)的文件夹的的相当于之前(this)的DirectorySnapshot有什么变化?
     * 需要去统计这期间发生了变更的文件列表以及操作类型(ADD/MODIFY/DELETE), 并将其包装到ChangedFiles
     *
     * @return 该目录下已经发生变更的文件列表(ChangedFiles)
     */
    open fun getChangedFiles(snapshot: DirectorySnapshot, triggerFilter: FileFilter?): ChangedFiles {
        val changes = ArrayList<ChangedFile>()
        // 获取之前的文件信息(key-File, value-FileSnapshot)
        val previousFiles = LinkedHashMap(getFileMap())
        snapshot.files.forEach {
            if (acceptChangedFile(triggerFilter, it)) {
                val previousFile = previousFiles.remove(it.file)

                // 如果之前还不存在这个文件的话, 说明这个文件是新添加的(ADD)
                if (previousFile == null) {
                    changes += ChangedFile(this.directory, it.file, ADD)

                    // 如果之前已经存在了当前的文件的话, 但是并不相等的话, 说明发生了改变(MODIFY)
                } else if (previousFile != it) {
                    changes += ChangedFile(this.directory, it.file, MODIFY)
                }
            }
        }

        // 如果还在previousFiles列表当中, 说明该文件之前在, 但是现在没了, (从有->无)说明该文件已经被删掉了(DELETE)
        previousFiles.values.forEach {
            if (acceptChangedFile(triggerFilter, it)) {
                changes += ChangedFile(this.directory, it.file, DELETE)
            }
        }

        // 返回当前文件夹下的所有的文件的变更情况
        return ChangedFiles(this.directory, changes)
    }

    /**
     * 收集该文件夹下的所有的文件的Snapshot信息
     *
     * @param source 要去收集文件Snapshot的文件夹
     * @param result 收集到的文件要存放到的集合, 对于收集到的FileSnapshot, 需要添加到这里
     */
    private fun collectFiles(source: File, result: MutableSet<FileSnapshot>) {
        source.listFiles()?.forEach { it ->
            // 如果它是一个文件的话, 那么直接去进行收集即可
            if (it.isFile) {
                result.add(FileSnapshot(it))

                // 如果它是一个文件夹, 那么递归去进行收集
            } else if (it.isDirectory && !DOTS.contains(it.name)) {
                collectFiles(it, result)
            }
        }
    }

    /**
     * 将FileSnapshot的List转换成为"Map(File, FileSnapshot)", 方便根据File去获取到FileSnapshot
     *
     * @return 转换后的FileMap(key-File,value-FileSnapshot)
     */
    private fun getFileMap(): Map<File, FileSnapshot> = this.files.map { it.file to it }.toMap(LinkedHashMap())

    /**
     * 比较之前的文件夹的snapshot和当前的snapshot(other)的触发的文件数量/内容相同?
     *
     * @param other newSnapShot
     * @param filter 触发文件的FileFilter(为null时, 所有的文件都会被当中触发文件)
     * @return 如果之前和之后的触发文件的内容不同则return false; 如果触发文件的内容相同, return true
     */
    open fun equals(@Nullable other: DirectorySnapshot?, filter: FileFilter? = null): Boolean {
        return this == other && filter(filter, other.files) == filter(filter, this.files)
    }

    /**
     * 使用filter从给定的FileSnapshot列表当中去过滤出来所有的触发文件
     *
     * @param filter filter(为null时, 所有的文件都会被当中触发文件)
     * @param source 要去进行匹配的FileSnapshot列表
     * @return 筛选出来所有的触发文件
     */
    private fun filter(filter: FileFilter?, source: Set<FileSnapshot>): Set<FileSnapshot> =
        if (filter == null) source else source.filter { filter.accept(it.file) }.toSet()

    /**
     * 判断是否需要接受该文件的改变?
     *
     * * 1.如果没有TriggerFilter, 那么只要文件改变了就接受;
     * * 2.如果指定了TriggerFilter, 那么触发的文件我们不应该作为ChangedFile去统计
     *
     * @param triggerFilter 触发的FileFilter
     * @param fileSnapshot 要去进行匹配的文件
     */
    private fun acceptChangedFile(triggerFilter: FileFilter?, fileSnapshot: FileSnapshot): Boolean {
        return triggerFilter == null || !triggerFilter.accept(fileSnapshot.file)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DirectorySnapshot
        if (directory != other.directory) return false
        if (files != other.files) return false
        return true
    }

    override fun hashCode(): Int {
        var result = directory.hashCode()
        result = 31 * result + files.hashCode()
        return result
    }
}