package com.github.fernthedev.fernutils.thread;


import com.github.fernthedev.fernutils.collections.ListUtils;
import com.github.fernthedev.fernutils.thread.multiple.TaskInfoFunctionList;
import com.github.fernthedev.fernutils.thread.multiple.TaskInfoList;
import com.github.fernthedev.fernutils.thread.multiple.TaskInfoSplitList;
import com.github.fernthedev.fernutils.thread.single.TaskInfo;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ThreadUtils {

    private static final ExecutorService cachedThreadExecutor = Executors.newCachedThreadPool();
    private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();


    @AllArgsConstructor
    public enum ThreadExecutors {
        CACHED_THREADS(cachedThreadExecutor),
        SINGLE_THREAD(singleThreadExecutor);

        @Getter
        private ExecutorService executorService;
    }

    /**
     * Runs the task async and sets the TaskInfo parameter of task to TaskInfo
     *
     * @param task     The task to run
     * @return The TaskInfo the Task is linked to
     */
    public static TaskInfo<Void> runAsync(Runnable task, ExecutorService executorService) {
        return runAsync(() -> {
            task.run();
            return null;
        }, executorService);
    }

    /**
     * Runs the task async and sets the TaskInfo parameter of task to TaskInfo
     *
     * @return The TaskInfo the Task is linked to
     */
    public static <R> TaskInfo<R> runAsync(Callable<R> callable, ExecutorService executorService) {
        return new TaskInfo<>(CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, executorService));
    }

    public static TaskInfoList runAsyncList(Collection<Runnable> runnables) {
        return new TaskInfoList(runnables.parallelStream().map(runnable -> (Callable<Void>) () -> {
            runnable.run();
            return null;
        }).collect(Collectors.toList()));
    }

    /**
     * @param dataList
     * @param function
     * @param <L>      the function parameter and List type
     *                 <p>
     *                 This handles creating tasks that provide the data from the list into the functions and store them in a list.
     * @return The {@link TaskInfoList} handles the threads and running the tasks.
     */
    public static <L> TaskInfoList runForLoopAsync(Collection<L> dataList, Function<L, ?> function) {
        List<Callable<Void>> callableList = dataList.parallelStream().map(l -> (Callable<Void>) () -> {
            function.apply(l);
            return null;
        }).collect(Collectors.toList());


        return new TaskInfoList(callableList);

//        return s;
    }

    /**
     * @param dataList
     * @param function
     * @param <L>      the function parameter and List type
     *                 <p>
     *                 This handles creating tasks that provide the data from the list into the functions and store them in a list.
     * @return The {@link TaskInfoList} handles the threads and running the tasks.
     */
    public static <L> TaskInfoList runForLoopAsync(Collection<L> dataList, Consumer<L> function) {
        List<Callable<Void>> callableList = dataList.parallelStream().map(l -> (Callable<Void>) () -> {
            function.accept(l);
            return null;
        }).collect(Collectors.toList());


        return new TaskInfoList(callableList);

//        return s;
    }

    /**
     * @param dataList
     * @param function
     * @param <L>      the function parameter and List type
     *                 <p>
     *                 This handles creating tasks that provide the data from the list into the functions and store them in a list.
     *
     * @param splitSize Splits the list into a specific amount for threads to handle.
     *
     *
     * @return The {@link TaskInfoList} handles the threads and running the tasks.
     */
    public static <L> TaskInfoSplitList runForLoopAsyncSplit(Collection<L> dataList, Consumer<L> function, int splitSize) {
        List<Callable<Void>> callableList = dataList.parallelStream().map(l -> (Callable<Void>) () -> {
            function.accept(l);
            return null;
        }).collect(Collectors.toList());

        List<List<Callable<Void>>> callableListSplit = ListUtils.splitList(callableList, splitSize);


        return new TaskInfoSplitList(callableListSplit);

//        return s;
    }

    /**
     * @param dataList
     * @param function
     * @param <L>      the function parameter and List type
     *                 <p>
     *                 This handles creating tasks that provide the data from the list into the functions and store them in a list.
     *
     * @param partitionSize Splits the list into a specific amount for threads to handle.
     *
     *
     * @return The {@link TaskInfoList} handles the threads and running the tasks.
     */
    public static <L> TaskInfoSplitList runForLoopAsyncParition(Collection<L> dataList, Consumer<L> function, int partitionSize) {
        List<Callable<Void>> callableList = dataList.parallelStream().map(l -> (Callable<Void>) () -> {
            function.accept(l);
            return null;
        }).collect(Collectors.toList());

        List<List<Callable<Void>>> callableListSplit = Lists.partition(callableList, partitionSize);


        return new TaskInfoSplitList(callableListSplit);

//        return s;
    }

    /**
     * @param dataList
     *                 <p>
     *                 This handles creating tasks that provide the data from the list into the functions and store them in a list.
     * @return The {@link TaskInfoList} handles the threads and running the tasks.
     */
    public static TaskInfoList runRunnablesAsync(Collection<Runnable> dataList) {
        return new TaskInfoList(dataList.parallelStream().map(runnable -> (Callable<Void>) () -> {
            runnable.run();
            return null;
        }).collect(Collectors.toList()));

//        return s;
    }

    /**
     * @param dataList
     * @param function
     * @param <L>      the function parameter and List type
     *                 <p>
     *                 This handles creating tasks that provide the data from the list into the functions and store them in a list.
     * @return The {@link TaskInfoFunctionList} handles the threads and providing parameters to the functions.
     */
    public static <L, R> TaskInfoFunctionList<L, R> runFunctionListAsync(Collection<L> dataList, Function<L, R> function) {

        List<Pair<L, Function<L, R>>> callableList = dataList.parallelStream().map(l ->
                new ImmutablePair<>(l, function)
        ).collect(Collectors.toList());

        return new TaskInfoFunctionList<>(callableList);
    }
}
