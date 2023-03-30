package com.github.fernthedev.fernutils.thread.multiple;

import com.github.fernthedev.fernutils.thread.impl.BaseMultiThreadedTaskInfo;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TaskInfoFunctionList<T, R> extends BaseMultiThreadedTaskInfo<
        List<Pair<T, Function<T, R>>>,
        List<CompletableFuture<Pair<T, R>>>,
        Map<T, R>> {

    private List<CompletableFuture<Pair<T, R>>> futureList;
    private List<Pair<T, Function<T, R>>> functionList;



//    private final Map<TaskFunction<T, R>, T> functionMap;
//
//    private Map<TaskFunction<T, R>, Thread> runningTasks = Collections.synchronizedMap(new HashMap<>());

    public TaskInfoFunctionList(List<Pair<T, Function<T, R>>> functionList) {
        this.functionList = functionList;

//        this.functionMap = new HashMap<>(functionTMap);
//        this.functionList = Collections.unmodifiableList(new ArrayList<>(functionTMap.keySet()));
    }

    /**
     *
     * @return The running tasks and their results
     */
    public List<CompletableFuture<Pair<T, R>>> runThreads(ExecutorService executor) throws InterruptedException {

        List<Callable<Pair<T, R>>> callableList = functionList.parallelStream()
                .map(tFunctionPair -> (Callable<Pair<T, R>>) () ->
                        new ImmutablePair<>(
                                tFunctionPair.getLeft(),
                                tFunctionPair.getRight().apply(tFunctionPair.getLeft()))
                ).collect(Collectors.toList());

        futureList = new ArrayList<>();



        callableList.forEach(pairCallable -> {
            CompletableFuture<Pair<T, R>> completableFuture = new CompletableFuture<>();
            futureList.add(completableFuture);

            executor.submit(() -> {
                try {
                    completableFuture.complete(pairCallable.call());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        });

        return futureList;

//        runningTasks = Collections.synchronizedMap(new HashMap<>());
//
//        Map<T, R> functionResults = new HashMap<>();
//
//        functionMap.forEach((function, key) -> {
//            Thread t = new Thread(() -> {
//                R result = function.run(TaskInfoFunctionList.this);
//
//                functionResults.put(key, result);
//            });
//
//            runningTasks.put(function, t);
//            t.start();
//        });
//
//        return functionResults;
    }

    @Override
    public List<Pair<T, Function<T, R>>> getTaskInstance() {
        return functionList;
    }

//
//    @Override
//    public void finish(TaskFunction<T, R> task) {
//        runningTasks.remove(task);
//    }


    private void checkStarted() {
        if (futureList == null) throw new IllegalStateException("The threads have not been started yet with runThreads();");
    }

    public void awaitFinish(int time) {
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


    public void join(int time) {
        awaitFinish(time);
    }

    public void interrupt() {
        checkStarted();

        futureList.parallelStream().forEach(trTaskFunction -> {
            trTaskFunction.cancel(true);
        });

    }

    @Override
    public Map<T, R> getValues() {
        checkStarted();

        return futureList.parallelStream().filter(Future::isDone).map(pairFuture -> {
            try {
                return Optional.ofNullable(pairFuture.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }).filter(Optional::isPresent).map(o -> (Pair<T, R>) o.get()).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    @Override
    public Map<T, R> getValuesAndAwait(int time) {
        awaitFinish(time);
        return getValues();
    }
}
