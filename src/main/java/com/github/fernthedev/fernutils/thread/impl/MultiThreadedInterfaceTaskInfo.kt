package com.github.fernthedev.fernutils.thread.impl

import java.util.concurrent.ExecutorService

/**
 * @param <T> Task type
 * @param <F> Task finish instance
 * @param <M> Multithreaded return
 */
interface MultiThreadedInterfaceTaskInfo<T, M, R> :
    InterfaceTaskInfo<T, R> {

    /**
     * Runs the threads and returns the result of each thread
     */
    @Throws(InterruptedException::class)
    fun runThreads(executor: ExecutorService): M



}