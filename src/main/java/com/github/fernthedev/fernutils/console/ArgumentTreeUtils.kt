package com.github.fernthedev.fernutils.console

import java.util.*
import javax.annotation.CheckReturnValue

/**
 * This is meant for parsing tree
 * arguments rather than a list
 * of arguments provided.
 */
object ArgumentTreeUtils {

    @JvmStatic
    fun parseArguments(args: Array<String>, vararg pair: Pair<String, (Queue<String>) -> Unit>, defaultArg: (Queue<String>) -> Unit = {}) {
        parseArguments(args.asList(), mapOf(*pair), defaultArg)
    }

    @JvmStatic
    fun parseArguments(args: Array<String>, map: Map<String, (Queue<String>) -> Unit>, defaultArg: (Queue<String>) -> Unit = {}) {
        parseArguments(args.asList(), map, defaultArg)
    }

    @JvmStatic
    fun parseArguments(args: List<String>, vararg pair: Pair<String, (Queue<String>) -> Unit>, defaultArg: (Queue<String>) -> Unit = {}) {
        parseArguments(args, mapOf(*pair), defaultArg)
    }

    /**
     * Convenience method for Java 9 using Map.entry()
     *
     */
    @JvmStatic
    fun parseArguments(args: Array<String>, vararg entry: Map.Entry<String, (Queue<String>) -> Unit>, defaultArg: (Queue<String>) -> Unit = {}) {
        val s: List<Pair<String, (Queue<String>) -> Unit>> = entry.map { it.toPair() };

        parseArguments(args, mapOf(*s.toTypedArray()), defaultArg)
    }

    /**
     * Convenience method for Java 9 using Map.entry()
     *
     */
    @JvmStatic
    fun parseArguments(args: List<String>, vararg entry: Map.Entry<String, (Queue<String>) -> Unit>, defaultArg: (Queue<String>) -> Unit = {}) {
        val s: List<Pair<String, (Queue<String>) -> Unit>> = entry.map { it.toPair() };

        parseArguments(args, mapOf(*s.toTypedArray()), defaultArg)
    }

    /**
     * Allow for parsing arguments in a friendly manner
     */
    @JvmStatic
    fun parseArguments(args: List<String>, map: Map<String, (Queue<String>) -> Unit>, defaultArg: (Queue<String>) -> Unit = {}) {
        val queue = LinkedList<String>(args);

        var found = false;
        
        for (s in args) {
            queue.pop()

            map[s]?.invoke(queue)

            if (map[s] != null) {
                found = true
                break
            }
        }
        
        if (!found)
            defaultArg.invoke(queue)
    }


    /**
     * Allow for parsing arguments in a friendly manner
     * This is more specific to Java
     * in using a builder pattern
     * as the Kotlin code is not very
     * friendly in Java
     */
    @JvmStatic
    @CheckReturnValue
    fun parseArguments(args: List<String>): JavaArgumentTreeHelper {
        return parseArguments(args.toTypedArray())
    }

    /**
     * Allow for parsing arguments in a friendly manner
     * This is more specific to Java
     * in using a builder pattern
     * as the Kotlin code is not very
     * friendly in Java
     */
    @JvmStatic
    @CheckReturnValue
    fun parseArguments(args: Array<String>): JavaArgumentTreeHelper {
        return JavaArgumentTreeHelper(args);
    }


}