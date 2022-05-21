package com.wanna.boot.autoconfigure

/**
 * 完成AutoConfiguration自动配置类的排序，主要处理AutoConfigureAfter/AutoConfigureBefore等注解
 */
open class AutoConfigurationSorter {

    /**
     * 完成自动配置类的排序...
     *
     * @param configurations 候选的配置类列表
     * @return 排好序的配置类列表
     */
    open fun sort(configurations: Collection<String>): Collection<String> {
        val result = ArrayList<String>()

        // 再检查AutoConfigureAfter去进行排序...
        configurations.forEach {
            val clazz = AutoConfigurationSorter::class.java.classLoader.loadClass(it)
            val after = clazz.getAnnotation(AutoConfigureAfter::class.java)
            if (after != null) {
                val names = ArrayList(after.name.toList())
                names += after.value.map { c -> c.java.name }.toList()

                // 先添加要after要导入的
                result -= names.toSet()
                result += names

                // 再添加this...
                result += it
            } else {
                if (!result.contains(it)) {
                    result += it
                }
            }
        }
        return LinkedHashSet(result)
    }
}