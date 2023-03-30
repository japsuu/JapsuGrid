package com.github.fernthedev.fernutils.thread.impl

/**
 * @param <T> Task type
 * @param <R> Return type
 */
interface InterfaceTaskInfo<T, R> {

    /**
     * Returns the task's instance
     */
    fun getTaskInstance() : T

    /**
     * Wait until the future has finished
     */
    fun awaitFinish(time: Int = 0)

    /**
     * Wait until Task's Thread finished
     */
    fun join(time: Int = 0)

    fun getValues() : R?


    fun getValuesAndAwait(time: Int = 0) : R? {
        awaitFinish(time)
        return getValues()
    }

    /**
     * Interrupts the task's thread
     */
    fun interrupt()
}