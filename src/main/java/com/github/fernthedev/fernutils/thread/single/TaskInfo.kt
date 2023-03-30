package com.github.fernthedev.fernutils.thread.single

import com.github.fernthedev.fernutils.thread.impl.BaseTaskInfo
import lombok.Data
import java.util.concurrent.CompletableFuture


@Data
open class TaskInfo<R>(private val task: CompletableFuture<R>) :
    BaseTaskInfo<CompletableFuture<R>, R>() {

    override fun getTaskInstance(): CompletableFuture<R> {
        return task
    }
    /**
     * Wait for the task to finsih
     */
    override fun awaitFinish(time: Int) {
        task.get()
    }

    @Throws(InterruptedException::class)
    override fun join(time: Int) {
        task.join()
    }

    override fun interrupt() {
        task.cancel(true)
    }

    override fun getValues(): R? {
        return task.getNow(null)
    }
}