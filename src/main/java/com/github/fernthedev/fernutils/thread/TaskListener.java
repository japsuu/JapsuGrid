package com.github.fernthedev.fernutils.thread;

import com.github.fernthedev.fernutils.thread.impl.InterfaceTaskInfo;

/**
 *
 * @param <T>
 * @param <R> Result
 */
public interface TaskListener<T extends InterfaceTaskInfo<?, R>, R> {

    void listen(T task, R taskResult);

}
