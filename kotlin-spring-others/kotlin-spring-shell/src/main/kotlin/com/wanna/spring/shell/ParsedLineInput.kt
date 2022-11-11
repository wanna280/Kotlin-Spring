package com.wanna.spring.shell

import com.wanna.framework.util.StringUtils

open class ParsedLineInput(private val str: String) : Input {

    override fun rawText() = str

    override fun words(): List<String> =
        StringUtils.commaDelimitedListToStringArray(str, " ").map { it.trim() }.toList()
}