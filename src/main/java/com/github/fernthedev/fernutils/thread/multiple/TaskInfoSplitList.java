package com.github.fernthedev.fernutils.thread.multiple;

import com.github.fernthedev.fernutils.thread.impl.BaseMultiThreadedTaskInfo;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class TaskInfoSplitList extends BaseMultiThreadedTaskInfo<
        List<List<Callable<Void>>>,
        List<CompletableFuture<Void>>,
        Void
        > {

    @Getter
    private List<CompletableFuture<Void>> futureList;

    private List<List<Callable<Void>>> callableList;

    public TaskInfoSplitList(List<List<Callable<Void>>> callableList) {
        this.callableList = callableList;
    }

    /**
     *
     * @return The running tasks and their results
     */
    @Override
    public List<CompletableFuture<Void>> runThreads(@NotNull ExecutorService executor) {

        futureList = new ArrayList<>();

        for (List<Callable<Void>> c : callableList) {
            futureList.add(CompletableFuture.runAsync(() -> {
                for (Callable<Void> callable : c) {
                    try {
                        callable.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, executor));
        }

        return futureList;
    }

    @Override
    public List<List<Callable<Void>>> getTaskInstance() {
        return callableList;
    }


    @Override
    public void awaitFinish(int time) {
        join(time);
    }


    private void checkStarted() {
        if (futureList == null) throw new IllegalStateException("The threads have not been started yet with runThreads();");
    }

    @Override
    public void join(int time) {
        checkStarted();

        futureList.parallelStream().forEach(trTaskFunction -> {
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
        futureList.parallelStream().forEach(trTaskFunction -> {
            trTaskFunction.cancel(true);
        });
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
