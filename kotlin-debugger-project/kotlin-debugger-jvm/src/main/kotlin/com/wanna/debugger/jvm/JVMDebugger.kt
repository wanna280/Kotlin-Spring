@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.wanna.debugger.jvm

import com.sun.jdi.*
import com.sun.jdi.connect.IllegalConnectorArgumentsException
import com.sun.jdi.event.BreakpointEvent
import com.sun.jdi.event.EventSet
import com.sun.jdi.event.StepEvent
import com.sun.jdi.event.VMDisconnectEvent
import com.sun.jdi.request.BreakpointRequest
import com.sun.jdi.request.EventRequest
import com.sun.jdi.request.StepRequest
import com.sun.tools.jdi.SocketAttachingConnector
import com.wanna.debugger.jvm.DebugInfo.ObjectInfo
import com.wanna.debugger.jvm.JVMDebugger.Companion.getDebugger
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import java.util.function.Predicate
import javax.annotation.Nullable

/**
 * JVM Debugger
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @param hostname 要去连接的目标VM主机名
 * @param port  要去连接的目标VM的JDWP端口号
 *
 * @see getDebugger
 */
open class JVMDebugger private constructor(private val hostname: String, private val port: Int) {
    /**
     * VirtualMachine to connect
     */
    private val virtualMachine: VirtualMachine

    /**
     * ThreadReference, 记录断点所停在的线程, 对于断点的操作归根到底都是对于单个线程的操作
     */
    @Nullable
    private var threadReference: ThreadReference? = null

    /**
     * 当前正在处理的EventSet
     */
    @Nullable
    private var currentEventSet: EventSet? = null

    /**
     * 当前正在处理的EventRequest(因为在处理后面的事件时, 需要将之前的事件给移除掉, 因此需要抽成为字段)
     */
    @Nullable
    private var currentEventRequest: EventRequest? = null

    init {
        // -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5000
        try {
            virtualMachine = connectToVM(hostname, port)
        } catch (ex: IllegalConnectorArgumentsException) {
            throw IllegalStateException("Illegal connector arguments", ex)
        } catch (ex: IOException) {
            throw IllegalStateException("Connect to VM(hostname=$hostname, port=$port) failed", ex)
        }
    }

    /**
     * 给指定的类的指定的行上去添加断点
     *
     * @param className  打断点的目标类
     * @param lineNumber 目标行号
     * @return DebugInfo
     */
    @Throws(Exception::class)
    fun breakpoint(className: String, lineNumber: Int): DebugInfo {
        createBreakpoint(className, lineNumber)
        return debugVM()
    }

    /**
     * 执行单步跳过
     *
     * @param stepType 单步类型(步入/步过/步出)
     * @return DebugInfo
     */
    @Throws(Exception::class)
    fun step(stepType: StepType): DebugInfo {
        createStepEvent(stepType)
        return debugVM()
    }

    /**
     * 和目标VM断开
     */
    private fun disconnect() {
        virtualMachine.dispose()
    }

    @Throws(Exception::class)
    private fun debugVM(): DebugInfo {
        val eventQueue = virtualMachine.eventQueue()

        // 如果没有事件触发, 那么在这里阻塞住...
        currentEventSet = eventQueue.remove()
        val debugInfo = DebugInfo()

        // 当有事件触发时, 就可以在这里去拿到具体的事件类型...
        val eventIterator = currentEventSet!!.eventIterator()
        if (eventIterator.hasNext()) {
            val event = eventIterator.next()
            // 如果是VM断开连接事件(对面VM关闭了...)
            if (event is VMDisconnectEvent) {
                debugInfo.isEnd = true
                return debugInfo

                // 如果是单步调试的事件的话...那么我们需要去处理断点事件
            } else if (event is StepEvent) {
                threadReference = event.thread()

                // 如果是断点事件的话...那么我们需要去处理断点事件
            } else if (event is BreakpointEvent) {
                threadReference = event.thread()
            }

            // 在拿到ThreadReference之后, 我们去对该线程的栈帧去进行处理...
            return try {
                val threadReference =
                    this.threadReference ?: throw IllegalStateException("threadReference cannot be null")

                // 获取顶层的栈帧去进行计算
                val stackFrame = threadReference.frame(0)
                val currentStackFrameLocation = stackFrame.location()
                val fields = currentStackFrameLocation.declaringType().allFields()
                val localVariables = stackFrame.visibleVariables()

                // 记录当前所在的类名/方法名/行号
                debugInfo.current = makeStackTraceElement(currentStackFrameLocation)

                // 记录整个线程栈的调用情况...
                for (frame in threadReference.frames()) {
                    debugInfo.stackTraces.add(makeStackTraceElement(frame.location()))
                }

                // 我们在这里去获取到ClassReference, 用于去统计static变量信息, 对于static变量都是通过Class去进行访问的
                for (field in fields) {
                    if (!field.isStatic) {
                        continue
                    }
                    val value = currentStackFrameLocation.declaringType().getValue(field)
                    val parsedValue = parseValue(value, 0)
                    debugInfo.staticObjects.add(ObjectInfo(field.name(), field.typeName(), parsedValue))
                }

                // 当前栈帧的this对象的相关信息(Note: 这里需要重新获取顶层栈帧, 因为上面计算static变量当前的线程栈已经执行过方法了, 会导致之前的栈帧已经被销毁)
                val currentObject = threadReference.frame(0).thisObject()

                // 如果this不为空, 那么计算当前栈帧的this对象的字段信息(如果this为空, 那么就算了, 不计算了)
                if (currentObject != null) {
                    for (field in fields) {
                        if (field.isStatic) {
                            continue
                        }
                        val value = currentObject.getValue(field)
                        val parsedValue = parseValue(value, 0)
                        debugInfo.fieldObjects.add(ObjectInfo(field.name(), field.typeName(), parsedValue))
                    }
                }

                // 计算当前栈帧的局部变量表的相关信息
                for (localVariable in localVariables) {
                    // Note: 这里需要重新获取顶层栈帧, 因为上面计算字段值导致该线程已经执行过方法了, 从而会导致之前的栈帧已经被销毁
                    val parsedValue = parseValue(threadReference.frame(0).getValue(localVariable), 0)
                    debugInfo.localVariables.add(
                        ObjectInfo(
                            localVariable.name(),
                            localVariable.typeName(),
                            parsedValue
                        )
                    )
                }
                debugInfo
            } catch (ex: Exception) {
                debugInfo.isEnd = true
                debugInfo
            }
        }
        return debugInfo
    }

    /**
     * 将给定的[value]的对象引用, 去解析成为真实的对象值
     *
     * @param value 待解析的对象引用ObjectReference
     * @param depth 当前深度
     * @return 解析完成得到的对象
     */
    @Throws(
        ClassNotLoadedException::class, IncompatibleThreadStateException::class,
        InvocationException::class, InvalidTypeException::class
    )
    @Nullable
    private fun parseValue(value: Value, depth: Int): Any? {
        if (value is IntegerValue) {
            return value.value()
        } else if (value is ByteValue) {
            return value.value()
        } else if (value is ShortValue) {
            return value.value()
        } else if (value is LongValue) {
            return value.value()
        } else if (value is BooleanValue) {
            return value.value()
        } else if (value is DoubleValue) {
            return value.value()
        } else if (value is FloatValue) {
            return value.value()
        } else if (value is CharValue) {
            return value.value()
        } else if (value is StringReference) {
            return value.value()

            // 如果是对象引用类型(ObjectReference), 那么需要进行解析...
        } else if (value is ObjectReference) {
            val toArrayMethod = COLLECTION_TO_ARRAY_METHOD_FUNC.apply(value)
            val entrySetMethod = MAP_ENTRY_SET_METHOD_FUNC.apply(value)

            // 如果是基础数据的包装类的话
            return if (PRIMITIVE_WRAPPER_PREDICATE.test(value.type())) {
                val field = value.referenceType().fieldByName("value")
                parseValue(value.getValue(field), depth)

                // 如果是Array的话, 那么直接可以解析Values数组...
            } else if (value is ArrayReference) {
                val list: MutableList<Any?> = ArrayList()
                for (elementValue in value.values) {
                    list.add(parseValue(elementValue, depth))
                }
                list

                // 如果是Collection的话, 通过解析toArray的结果去完成...
            } else if (isCollection(value.type()) && toArrayMethod != null) {
                val collectionArray = value.invokeMethod(threadReference, toArrayMethod, emptyList(), 0)
                parseValue(collectionArray, depth)

                // 如果是Map的话, 通过解析entrySet的方式去完成...
            } else if (isMap(value.type()) && entrySetMethod != null) {
                val mapEntrySet = value.invokeMethod(threadReference, entrySetMethod, emptyList(), 0)
                parseValue(mapEntrySet, depth)

                // 如果是其他类型的ObjectReference的话
            } else {
                val result: MutableMap<String, Any?> = LinkedHashMap()
                val className = value.referenceType().name()
                result["className"] = className

                // 如果是MapEntry, 那么只去计算Key/Value信息就足够了...别的字段别计算了
                if (isMapEntry(value.type())) {
                    val keyField = value.referenceType().fieldByName("key")
                    val valueField = value.referenceType().fieldByName("value")
                    result["key"] = parseValue(value.getValue(keyField), depth)
                    result["value"] = parseValue(value.getValue(valueField), depth)
                    return result
                }
                if (className == "java.lang.Object") {
                    return result
                }
                try {
                    val fields = value.referenceType().allFields()
                    for (field in fields) {
                        // 不去计算static字段和合成字段
                        if (field.isStatic || field.isSynthetic) {
                            continue
                        }

                        // Note: 这里需要去作为递归的终止条件, 待优化
                        if (depth >= 2) {
                            continue
                        }
                        val fieldValue = value.getValue(field)
                        result[field.name()] = parseValue(fieldValue, depth + 1)
                    }
                    result
                } catch (ex: Exception) {
                    result
                }
            }
        }
        return null
    }

    /**
     * 检查给定的[type]是否是一个Map? 支持去寻找该类的所有的接口
     *
     * @param type type
     * @return 如果类型是Map的话, return true
     */
    private fun isMap(type: Type): Boolean {
        return isAncestor(type, "java.util.Map")
    }

    /**
     * 检查给定的[type]是否是一个MapEntry? 支持去寻找该类的所有的接口
     *
     * @param type type
     * @return 如果类型是MapEntry的话, return true
     */
    private fun isMapEntry(type: Type): Boolean {
        return isAncestor(type, "java.util.Map\$Entry")
    }

    /**
     * 检查给定的[type]是否是一个Collection? 支持去寻找该类的所有的接口
     *
     * @param type type
     * @return 如果类型是Collection的话, return true
     */
    private fun isCollection(type: Type): Boolean {
        return isAncestor(type, "java.util.Collection")
    }

    /**
     * 检查目标Type的所有接口、所有父类当中, 是否存在有和[assignTo]相符合的类?
     *
     * @param type     待检查的目标Type
     * @param assignTo 待比对的类名
     * @return 如果给定的Type存在有给定assignTo类名去作为父类/接口, return true; 否则return false
     */
    private fun isAncestor(@Nullable type: Type?, @Nullable assignTo: String?): Boolean {
        if (type === null || assignTo === null) {
            return false
        }
        if (type.name() == assignTo) {
            return true
        }
        if (type is ClassType) {

            // 检查所有的接口, 去进行递归检查...
            for (interfaceType in type.interfaces()) {
                if (isAncestor(interfaceType, assignTo)) {
                    return true
                }
            }

            // 检查父类, 去进行递归检查...
            val superclass = type.superclass()
            return isAncestor(superclass, assignTo)
        }
        return false
    }

    /**
     * 针对给定的代码位置[Location], 去生成当前位置的
     */
    private fun makeStackTraceElement(location: Location): DebugInfo.StackTraceElement {
        return DebugInfo.StackTraceElement(
            location.declaringType().name(),
            location.method().name(),
            location.lineNumber()
        )
    }

    /**
     * 创建一个单步调试的事件, 为VM添加一个[StepRequest]
     *
     * @param stepType StepType(步入/步过/步出)
     */
    private fun createStepEvent(stepType: StepType) {
        // 获取到目标VM的RequestHandler
        val eventRequestManager = virtualMachine.eventRequestManager()

        // Note: 先把之前的request移除掉, 不然重复添加EventRequest会出现"Only one step request allowed per thread"异常
        if (currentEventRequest != null) {
            eventRequestManager.deleteEventRequest(currentEventRequest)
        }
        val eventRequest =
            eventRequestManager.createStepRequest(threadReference, StepRequest.STEP_LINE, stepType.index)
        eventRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD)
        eventRequest.enable()

        this.currentEventRequest = eventRequest

        // 如果存在有正在处理的断点事件, 那么需要将该事件先去释放掉, 不然当前断点事件卡不住
        if (currentEventSet != null) {
            currentEventSet!!.resume()
        }
    }

    /**
     * 给VM的指定类上添加断点, 添加一个[BreakpointRequest],
     * 创建断点会挂起目标VM所有的线程, 因此如果不使用断点的话, 那么需要将断点去释放...
     *
     * @param className  要打断点的类的className
     * @param lineNumber 要打断点的行号
     */
    @Throws(AbsentInformationException::class)
    private fun createBreakpoint(className: String, lineNumber: Int) {
        // 获取到目标VM的RequestHandler
        val eventRequestManager = virtualMachine.eventRequestManager()

        // Note: 先把之前的request移除掉, 不然重复添加EventRequest会出现"Only one step request allowed per thread"异常
        if (currentEventRequest != null) {
            eventRequestManager.deleteEventRequest(currentEventRequest)
        }

        // 根据ClassName, 从VM当中去寻找到ReferenceTypes
        val referenceTypes = virtualMachine.classesByName(className)
        check(!(Objects.isNull(referenceTypes) || referenceTypes.isEmpty())) { "Cannot find target Class $className" }
        val classType = referenceTypes[0] as ClassType

        // 根据行号, 找到该类的对应行号的位置
        val locations = classType.locationsOfLine(lineNumber)
        check(!(Objects.isNull(locations) || locations.isEmpty())) { "Cannot find target Location for given lineNumber $lineNumber in $className" }
        val location = locations[0]

        // 为给定的ClassName、给定的LineNumber去打上断点...
        val eventRequest = eventRequestManager.createBreakpointRequest(location)

        // 设定挂起策略(挂起全部/挂起处理请求的目标线程/不挂起), 对于设置断点来说, 我们选择去挂起所有的线程...
        eventRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL)
        eventRequest.enable()

        this.currentEventRequest = eventRequest

        // 如果存在有正在处理的断点事件, 那么需要将该事件先去释放掉, 不然当前断点事件卡不住
        if (currentEventSet != null) {
            currentEventSet!!.resume()
        }
    }

    /**
     * 连接到目标JVM
     *
     * @param hostname hostname
     * @param port     port
     * @return 连接的目标VM
     *
     * @throws IllegalConnectorArgumentsException 连接参数错误
     * @throws IOException                        如果连接VM失败
     */
    @Throws(IllegalConnectorArgumentsException::class, IOException::class)
    private fun connectToVM(hostname: String, port: Int): VirtualMachine {
        val virtualMachineManager = Bootstrap.virtualMachineManager()
        val attachingConnectors = virtualMachineManager.attachingConnectors()
        var socketAttachingConnector: SocketAttachingConnector? = null

        // 这里有两类Connector, 一类是基于Process, 也就是进程PID的; -->com.sun.tools.jdi.ProcessAttachingConnector
        // 另外一类是基于Socket的, 基于Socket实现通信 -->com.sun.tools.jdi.SocketAttachingConnector
        for (attachingConnector in attachingConnectors) {
            if (attachingConnector is SocketAttachingConnector) {
                socketAttachingConnector = attachingConnector
            }
        }
        checkNotNull(socketAttachingConnector) { "Cannot find SocketAttachingConnector" }
        val args = socketAttachingConnector.defaultArguments()
        args["hostname"]!!.setValue(hostname)
        args["port"]!!.setValue(port.toString())
        return socketAttachingConnector.attach(args)
    }

    companion object {
        /**
         * 寻找到Map类当中的entrySet方法的函数
         */
        @JvmStatic
        private val MAP_ENTRY_SET_METHOD_FUNC =
            Function<ObjectReference, Method?> { value ->
                val methods = value.referenceType().methodsByName("entrySet")
                for (method in methods) {
                    try {
                        if (method.argumentTypes().isEmpty()) {
                            return@Function method
                        }
                    } catch (ex: ClassNotLoadedException) {
                        // ignore
                    }
                }
                return@Function null
            }

        /**
         * 寻找到Collection类当中的toArray方法的函数
         */
        @JvmStatic
        private val COLLECTION_TO_ARRAY_METHOD_FUNC =
            Function<ObjectReference, Method?> { value ->
                val methods = value.referenceType().methodsByName("toArray")
                for (method in methods) {
                    try {
                        if (method.argumentTypes().isEmpty()) {
                            return@Function method
                        }
                    } catch (ex: ClassNotLoadedException) {
                        // ignore
                    }
                }
                return@Function null
            }

        /**
         * 基础数据类型的包装类型的断言匹配
         */
        @JvmStatic
        private val PRIMITIVE_WRAPPER_PREDICATE =
            Predicate<Type> { type ->
                type.name() == "java.lang.Integer"
                        || type.name() == "java.lang.Byte"
                        || type.name() == "java.lang.Short"
                        || type.name() == "java.lang.Long"
                        || type.name() == "java.lang.Char"
                        || type.name() == "java.lang.Boolean"
                        || type.name() == "java.lang.Float"
                        || type.name() == "java.lang.Double"
            }

        /**
         * Debugger Cache, Key-Tag, Value-JVMDebugger
         */
        @JvmStatic
        private val DEBUGGER_CACHE: MutableMap<String, JVMDebugger> = ConcurrentHashMap()

        /**
         * 获取JVMDebugger的工厂方法
         *
         * @param tag      tag
         * @param hostname hostname
         * @param port     port
         * @return JVMDebugger
         */
        @Nullable
        @JvmStatic
        fun getDebugger(tag: String, hostname: String, port: Int): JVMDebugger? {
            var debugger = getDebugger(tag)
            if (Objects.isNull(debugger)) {
                synchronized(DEBUGGER_CACHE) {
                    debugger = JVMDebugger(hostname, port)
                    DEBUGGER_CACHE.put(tag, debugger!!)
                }
            }
            return getDebugger(tag)
        }

        /**
         * 根据tag去获取JVMDebugger的工厂方法
         *
         * @param tag tag
         * @return JVMDebugger
         */
        @Nullable
        @JvmStatic
        fun getDebugger(tag: String): JVMDebugger? = DEBUGGER_CACHE[tag]

        /**
         * 根据tag去获取到JVMDebugger的工厂方法
         *
         * @param tag tag
         * @return DebugInfo
         */
        @JvmStatic
        fun removeDebugger(tag: String): DebugInfo {
            val debugInfo = DebugInfo()
            debugInfo.isEnd = true
            if (tag.isEmpty()) {
                return debugInfo
            }
            val debugger = getDebugger(tag) ?: return debugInfo
            debugger.disconnect()
            DEBUGGER_CACHE.remove(tag)
            return debugInfo
        }
    }
}