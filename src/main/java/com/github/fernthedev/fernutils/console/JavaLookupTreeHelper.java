package com.github.fernthedev.fernutils.console;

import kotlin.jvm.functions.Function1;
import lombok.NonNull;

import javax.annotation.CheckReturnValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

public class JavaLookupTreeHelper<V> {

    private final String[] args;
    private final Map<String, Function<Queue<String>, V>> finderMap = new HashMap<>();

    @NonNull
    private Function<Queue<String>, V> defaultHandleFunc = (queue -> null) ;

    public JavaLookupTreeHelper(String[] args) {
        this.args = args;
    }

    /**
     * Handles the argument
     * @param arg the arg checked to call the function
     * @param queue the function that is called when arg is detected
     *
     */
    @CheckReturnValue
    public JavaLookupTreeHelper<V> handle(String arg, Function<Queue<String>, V> queue) {
        finderMap.put(arg, queue);

        return this;
    }

    /**
     * Invokes the default
     * if none of the handles are found
     *
     * @return
     */
    public JavaLookupTreeHelper<V> defaultHandle(Function<Queue<String>, V> queue) {
        if (queue != null) this.defaultHandleFunc = queue;
        else this.defaultHandleFunc = (q -> null);

        return this;
    }

    public V apply() {
        Map<String, Function1<Queue<String>, V>> newMap = new HashMap<>();

        finderMap.forEach((s, queueConsumer) -> newMap.put(s, queueConsumer::apply));

        Function1<? super Queue<String>, V> defHandle = (Function1<Queue<String>, V>) queue -> defaultHandleFunc.apply(queue);

        return LookupTreeUtils.parseArguments(args, newMap, defHandle);
    }

}
