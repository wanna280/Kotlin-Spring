package com.wanna.logger.impl.encoder.support

import com.wanna.logger.impl.event.ILoggingEvent

open class PatternLayoutEncoder : PatternLayoutEncoderBase<ILoggingEvent>() {

    override fun encode(e: ILoggingEvent): String {
        val layout = getLayout() ?: throw IllegalStateException("请先初始化Layout")
        return layout.doLayout(e)
    }
}