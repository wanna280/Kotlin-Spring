package com.wanna.boot.actuate.endpoint.invoke

import java.util.stream.Stream

interface OperationParameters : Iterable<OperationParameter> {

    fun hasParameters(): Boolean = getParameterCount() > 0

    fun getParameterCount(): Int

    fun get(index: Int): OperationParameter

    fun stream(): Stream<OperationParameter>
}