package com.wanna.boot.context.config

import com.wanna.boot.origin.Origin
import com.wanna.boot.origin.OriginTrackedResource

/**
 * 标注的ConfigDataLoader的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @see StandardConfigDataResource
 * @see StandardConfigDataReference
 */
open class StandardConfigDataLoader : ConfigDataLoader<StandardConfigDataResource> {

    /**
     * 执行对于给定的StandardContextDataResource的加载
     *
     * @param context context
     * @param resource resource
     * @return 加载Resource得到的PropertySource的结果封装得到的ConfigData
     */
    override fun load(context: ConfigDataLoaderContext, resource: StandardConfigDataResource): ConfigData {
        if (resource.emptyDirectory) {
            return ConfigData.EMPTY
        }
        val reference = resource.reference

        // 根据原始的Resource去构建出来一个OriginTrackedResource
        val originTrackedResource =
            OriginTrackedResource.of(resource.resource, Origin.from(reference.configDataLocation))


        // format PropertySource name
        val name = String.format("Config resource '%s' via location '%s'", resource, reference.configDataLocation)

        // 利用PropertySourceLoader去加载该资源, 得到PropertySource
        val propertySources = reference.propertySourceLoader.load(name, originTrackedResource)
        return ConfigData(propertySources)
    }
}