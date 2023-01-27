package com.wanna.boot.devtools.classpath

import com.wanna.boot.devtools.filewatch.ChangedFile
import com.wanna.framework.util.AntPathMatcher
import com.wanna.framework.util.StringUtils

/**
 * 基于Ant表达式的方式, 去比较当中一个文件发生变更时, 是否应该去进行重启
 *
 * @param excludePatterns 要去进行排除的表达式, 这些表达式的路径, 将会不被重启
 */
class PatternClassPathRestartStrategy(private val excludePatterns: Array<String>) : ClassPathRestartStrategy {

    /**
     * 提供一个字符串的形式的表达式的构造器
     */
    constructor(excludePatterns: String) : this(StringUtils.commaDelimitedListToStringArray(excludePatterns))

    /**
     * Ant PathMatcher
     */
    private val pathMatcher = AntPathMatcher()

    /**
     * 当[changedFile]命中[excludePatterns]当中的其中一条规则, 就不需要去进行重启, 否则则需要去进行重启
     *
     * @param changedFile 发生变更的文件
     * @return 如果该文件的路径在[excludePatterns]当中, 那么return false不需要重启; 否则return true需要重启
     */
    override fun isRestartRequired(changedFile: ChangedFile): Boolean {
        excludePatterns.forEach {
            if (pathMatcher.match(it, changedFile.file.path)) {
                return false
            }
        }
        return true
    }
}