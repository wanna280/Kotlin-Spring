package com.wanna.framework.scheduling.config

import java.util.concurrent.Future

class ScheduledTask(val task: Task, var future: Future<*>? = null) {

    fun cancel() {
        if (this.future != null) {
            this.future!!.cancel(true)
        }
    }
}