package com.wanna.framework.validation.annotation

import kotlin.reflect.KClass

annotation class Validated(vararg val value: KClass<*>)
