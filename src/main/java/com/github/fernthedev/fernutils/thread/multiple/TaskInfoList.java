package com.github.fernthedev.fernutils.thread.multiple;

import com.github.fernthedev.fernutils.thread.impl.BaseMultiThreadedTaskInfo;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class TaskInfoList extends BaseMultiThreadedTaskInfo<
        List<Callable<Void>>,
        List<CompletableFuture<Void>>,
        Void
        > {

    @Getter
    private List<CompletableFuture<Void>> future;

    private List<Callable<Void>> callableList;

    public TaskInfoList(List<Callable<Void>> callableList) {
        this.callableList = callableList;
    }

    /**
     *
     * @return The running tasks and their results
     */
    @Override
    public List<CompletableFuture<Void>> runThreads(ExecutorService executor) {
        future = new ArrayList<>();

        callableList.forEach(voidCallable -> future.add(CompletableFuture.runAsync(() -> {
            try {
                voidCallable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor)));

        return future;
    }

    @Override
    public List<Callable<Void>> getTaskInstance() {
        return callableList;
    }


    @Override
    public void awaitFinish(int time) {
        join(time);
    }


    @Override
    public void join(int time) {
        if (future == null) throw new IllegalStateException("The threads have not been started yet with runThreads();");

        future.parallelStream().forEach(trTaskFunction -> {
            try {
                trTaskFunction.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void interrupt() {
        future.parallelStream().forEach(trTaskFunction -> trTaskFunction.cancel(true));
    }

    @Override
    public Void getValues() {
        return null;
    }

    @Override
    public Void getValuesAndAwait(int time) {
        awaitFinish(time);
        return getValues();
    }
}
