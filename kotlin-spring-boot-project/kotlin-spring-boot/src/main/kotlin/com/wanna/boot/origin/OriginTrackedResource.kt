package com.wanna.boot.origin

import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.WritableResource
import com.wanna.framework.lang.Nullable
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.net.URL

/**
 * 带有Origin的追踪功能的Resource
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
open class OriginTrackedResource(private val resource: Resource, @Nullable private val origin: Origin?) : Resource,
    OriginProvider {

    @Nullable
    override fun getOrigin(): Origin? = origin

    override fun getInputStream(): InputStream = resource.getInputStream()

    override fun exists(): Boolean = resource.exists()

    override fun getURI(): URI = resource.getURI()

    override fun getURL(): URL = resource.getURL()

    override fun getFile(): File = resource.getFile()
    override fun lastModified(): Long = resource.lastModified()

    override fun contentLength(): Long = resource.contentLength()

    override fun createRelative(relativePath: String): Resource = resource.createRelative(relativePath)

    @Nullable
    override fun getFilename(): String? = resource.getFilename()

    @Nullable
    override fun getDescription(): String? = resource.getDescription()

    open fun getResource(): Resource = this.resource

    companion object {

        /**
         * 返回一个根据给定的[Resource]去构建出来的[OriginTrackedResource]
         *
         * @param resource Resource
         * @param origin Origin
         * @return OriginTrackedResource
         */
        @JvmStatic
        fun of(resource: Resource, @Nullable origin: Origin?): OriginTrackedResource {
            if (resource is WritableResource) {
                return OriginTrackedWritableResource(resource, origin)
            }
            return OriginTrackedResource(resource, origin)
        }

        /**
         * 返回一个根据[WritableResource]去构建出来的[OriginTrackedWritableResource]
         *
         * @param resource WritableResource
         * @param origin Origin
         * @return OriginTrackedWritableResource
         */
        @JvmStatic
        fun of(resource: WritableResource, @Nullable origin: Origin?): OriginTrackedWritableResource {
            return of(resource as Resource, origin) as OriginTrackedWritableResource
        }
    }

    /**
     * 带Origin追踪的WritableResource
     */
    open class OriginTrackedWritableResource(resource: WritableResource, @Nullable origin: Origin?) :
        OriginTrackedResource(resource, origin),
        WritableResource {
        override fun isWritable(): Boolean = (getResource() as WritableResource).isWritable()
        override fun getOutputStream(): OutputStream = (getResource() as WritableResource).getOutputStream()
    }
}