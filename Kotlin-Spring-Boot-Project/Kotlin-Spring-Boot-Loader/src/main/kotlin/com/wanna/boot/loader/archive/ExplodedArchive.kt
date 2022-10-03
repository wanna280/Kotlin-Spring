package com.wanna.boot.loader.archive

import java.io.File
import java.net.URL
import java.util.jar.Manifest

/**
 * 这是一个被解压之后得到的Java归档文件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 */
open class ExplodedArchive(val root: File) : Archive {


    override fun iterator(): Iterator<Archive.Entry> {
        TODO("Not yet implemented")
    }

    override fun getUrl(): URL {
        TODO("Not yet implemented")
    }

    override fun getManifest(): Manifest {
        TODO("Not yet implemented")
    }

    override fun getNestedArchives(
        searchFilter: Archive.EntryFilter,
        includeFilter: Archive.EntryFilter
    ): Iterator<Archive> {
        TODO("Not yet implemented")
    }
}