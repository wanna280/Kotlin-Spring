package com.wanna.boot.actuate.metrics

import com.wanna.boot.actuate.endpoint.InvalidEndpointRequestException
import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.ReadOperation
import com.wanna.boot.actuate.endpoint.annotation.Selector
import com.wanna.framework.lang.Nullable
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Statistic
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import java.util.*
import java.util.function.BiFunction
import java.util.function.Consumer

/**
 * 提供对于Metrics相关的Endpoint的暴露
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/29
 */
@Endpoint("metrics")
open class MetricsEndpoint(private val meterRegistry: MeterRegistry) {

    /**
     * 列举出来当前应用当中的所有的指标名的列表
     *
     * @return merge得到所有的指标名的列表
     */
    @ReadOperation
    open fun listNames(): ListNamesResponse {
        val names = TreeSet<String>()

        // 从MeterRegistry当中去收集起来所有的指标名
        collectNames(names, this.meterRegistry)
        // 包装成为对象去进行返回
        return ListNamesResponse(names)
    }

    /**
     * 根据对应的指标名去获取到对应的指标信息(例如URL:"/actuator/metrics/jvm.buffer.memory.used?tag=id:direct")
     *
     * @param requiredMetricName 指标名(通过路径变量传递过来, 例如"jvm.buffer.memory.used")
     * @param tag tag(可以为null, 例如"tag=id:direct")
     * @return 想要获取的指标的结果(MetricResponse, 收集不到return null)
     */
    @ReadOperation
    open fun metric(@Selector requiredMetricName: String, @Nullable tag: List<String>?): MetricResponse? {
        val tags = parseTags(tag)

        // 找到所有的MeterRegistry当中第一个找到了合适的Meter指标参数的结果
        val meters = findFirstMatchingMeters(this.meterRegistry, tags, requiredMetricName)

        // 如果没有找到结果的话，那么我们这里return null
        if (meters.isEmpty()) {
            return null
        }

        val samples = getSamples(meters)
        val availableTags = getAvailableTags(meters)

        // remove something
        availableTags.forEach { it.value.remove(it.key) }
        val meterId = meters.first().id

        // 将结果去进行Merge，得到MetricsResponse
        return MetricResponse(
            requiredMetricName,  // name
            meterId.description,  // description
            meterId.baseUnit,  // baseUnit
            asList(samples) { t, u -> Sample(t, u) },  // transform to Samples
            asList(availableTags) { t, u -> AvailableTag(t, u) } // transform to AvailTags
        )
    }

    private fun <K, V, T> asList(map: Map<K, V>, mapper: BiFunction<K, V, T>): List<T> {
        return map.entries.map { mapper.apply(it.key, it.value) }.toList()
    }

    /**
     * 获取到所有的Sample信息
     *
     * @param meters meters
     * @return Samples
     */
    private fun getSamples(meters: Collection<Meter>): Map<Statistic, Double> {
        val samples = LinkedHashMap<Statistic, Double>()
        meters.forEach { mergeMeasurements(samples, it) }
        return samples
    }

    /**
     * Merge所有的测量指标
     *
     * @param samples samples(输出参数)
     * @param meter meter
     */
    private fun mergeMeasurements(samples: MutableMap<Statistic, Double>, meter: Meter) {
        meter.measure().forEach { samples.merge(it.statistic, it.value, mergeFunction(it.statistic)) }
    }

    /**
     * 用于merge的函数
     */
    private fun mergeFunction(statistic: Statistic): BiFunction<Double, Double, Double> =
        if (Statistic.MAX == statistic) BiFunction { a, b -> maxOf(a, b) } else BiFunction { a, b -> a + b }

    /**
     * 获取所有的可用的Tags
     */
    private fun getAvailableTags(meters: Collection<Meter>): MutableMap<String, MutableSet<String>> {
        val availableTags: MutableMap<String, MutableSet<String>> = HashMap()
        meters.forEach(Consumer { meter: Meter -> mergeAvailableTags(availableTags, meter) })
        return availableTags
    }

    /**
     * Merge所有可用的Tags
     */
    private fun mergeAvailableTags(availableTags: MutableMap<String, MutableSet<String>>, meter: Meter) {
        meter.id.tags.forEach(Consumer { tag ->
            val value = mutableSetOf(tag.value)
            availableTags.merge(tag.key, value) { set1, set2 -> HashSet(set1 + set2) }
        })
    }

    /**
     * 寻找第一个匹配的指标(如果有多个MeterRegistry，那么只去寻找到第一个合适的元素即结束)
     *
     * @param registry MeterRegistry
     * @param tags tags
     * @param name name
     * @return Meters
     */
    private fun findFirstMatchingMeters(registry: MeterRegistry, tags: List<Tag>, name: String): Collection<Meter> {
        if (registry is CompositeMeterRegistry) {
            return registry.registries.stream()
                .map { findFirstMatchingMeters(it, tags, name) }
                .filter { !it.isEmpty() }
                .findFirst().orElse(emptyList())

        }
        return registry.find(name).tags(tags).meters()
    }


    /**
     * 解析得到Tags列表
     *
     * @param tags tagStr("key:value")
     * @return Tags列表
     */
    private fun parseTags(tags: List<String>?): List<Tag> {
        return tags?.map(this::parseTag)?.toList() ?: emptyList()
    }

    /**
     * 解析Tag
     *
     * @param tag tagStr("key:value")
     * @return Tag
     */
    private fun parseTag(tag: String): Tag {
        val parts = tag.split(":")
        if (parts.size != 2) {
            throw InvalidEndpointRequestException("给定的Tag必须是key:value的格式, 但是给定的是:[$tag]", "给定的Tag必须是key:value的格式")
        }
        return Tag.of(parts[0], parts[1])
    }


    /**
     * 收集出来所有的指标名，并merge到`names`这个列表当中来
     *
     * @param names 输出参数，将数据merge到这里
     * @param registry 提供收集指标名的MeterRegistry
     */
    private fun collectNames(names: MutableSet<String>, registry: MeterRegistry) {

        // 如果是一个CompositeMeterRegistry，那么递归处理所有内部的MeterRegistry
        if (registry is CompositeMeterRegistry) {
            registry.registries.forEach { collectNames(names, it) }

            // 如果是一个put的MeterRegistry的话，那么将内部所有的meter去merge到names列表当中来
        } else {
            registry.meters.stream().map(this::getName).forEach(names::add)
        }
    }

    /**
     * 从[Meter]当中去获取到指标名
     *
     * @param meter Meter
     * @return 指标名
     */
    private fun getName(meter: Meter): String = meter.id.name


    /**
     * 对于所有的监控指标的指标名的统计
     *
     * @param names 监控指标名列表
     */
    data class ListNamesResponse(val names: Set<String>)

    /**
     * 获取一个具体的指标的响应结果
     *
     * @param name 指标名
     * @param description 指标的描述信息
     */
    data class MetricResponse(
        val name: String,
        val description: String?,
        val baseUnit: String?,
        val measurements: List<Sample>,
        val tags: List<AvailableTag>
    )

    data class AvailableTag(val tag: String, val values: Set<String>)

    data class Sample(val statistic: Statistic, val value: Double)
}