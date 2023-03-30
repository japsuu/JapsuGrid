package com.github.fernthedev.fernutils.console;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import lombok.NonNull;

import javax.annotation.CheckReturnValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

public class JavaArgumentTreeHelper {

    private final String[] args;
    private final Map<String, Consumer<Queue<String>>> finderMap = new HashMap<>();

    @NonNull
    private Consumer<Queue<String>> defaultHandleFunc = (queue -> {}) ;

    public JavaArgumentTreeHelper(String[] args) {
        this.args = args;
    }

    /**
     * Handles the argument
     * @param arg the arg checked to call the consumer
     * @param queue the consumer that is called when arg is detected
     *
     */
    @CheckReturnValue
    public JavaArgumentTreeHelper handle(String arg, Consumer<Queue<String>> queue) {
        finderMap.put(arg, queue);

        return this;
    }

    /**
     * Invokes the default
     * if none of the handles are found
     *
     * @return
     */
    public JavaArgumentTreeHelper defaultHandle(Consumer<Queue<String>> queue) {
        if (queue != null) this.defaultHandleFunc = queue;
        else this.defaultHandleFunc = (q -> {});

        return this;
    }

    public void apply() {
        Map<String, Function1<Queue<String>, Unit>> newMap = new HashMap<>();

        finderMap.forEach((s, queueConsumer) -> newMap.put(s, strings -> {
            queueConsumer.accept(strings);
            return Unit.INSTANCE;
        }));

        Function1<? super Queue<String>, Unit> defHandle = (Function1<Queue<String>, Unit>) queue -> {
            defaultHandleFunc.accept(queue);
            return Unit.INSTANCE;
        };

        ArgumentTreeUtils.parseArguments(args, newMap, defHandle);

    }

}
