package com.wanna.middleware.cli.impl

import com.wanna.middleware.cli.Argument

/**
 * 命令行参数的比较器, 按照index去对参数去进行排序
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/27
 */
object ArgumentComparator : Comparator<Argument> {
    override fun compare(o1: Argument, o2: Argument): Int {
        return if (o1.getIndex() == o2.getIndex()) 1 else o1.getIndex().compareTo(o2.getIndex())
    }
}