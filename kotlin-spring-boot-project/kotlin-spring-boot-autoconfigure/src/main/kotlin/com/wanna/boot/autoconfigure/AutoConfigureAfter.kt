package com.wanna.boot.autoconfigure

import kotlin.reflect.KClass

annotation class AutoConfigureAfter(
    val value: Array<KClass<*>> = [],
    val name: Array<String> = []
)
