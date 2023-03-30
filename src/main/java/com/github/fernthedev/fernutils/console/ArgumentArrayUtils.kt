package com.github.fernthedev.fernutils.console

import java.util.*
import javax.annotation.CheckReturnValue

object ArgumentArrayUtils {

    @JvmStatic
    fun parseArguments(args: Array<String>, vararg pair: Pair<String, (Queue<String>) -> Unit>) {
        parseArguments(args.asList(), mapOf(*pair))
    }

    @JvmStatic
    fun parseArguments(args: Array<String>, map: Map<String, (Queue<String>) -> Unit>) {
        parseArguments(args.asList(), map)
    }

    @JvmStatic
    fun parseArguments(args: List<String>, vararg pair: Pair<String, (Queue<String>) -> Unit>) {
        parseArguments(args, mapOf(*pair))
    }

    /**
     * Convenience method for Java 9 using Map.entry()
     *
     */
    @JvmStatic
    fun parseArguments(args: Array<String>, vararg entry: Map.Entry<String, (Queue<String>) -> Unit>) {
        val s: List<Pair<String, (Queue<String>) -> Unit>> = entry.map { it.toPair() };

        parseArguments(args, mapOf(*s.toTypedArray()))
    }

    /**
     * Convenience method for Java 9 using Map.entry()
     *
     */
    @JvmStatic
    fun parseArguments(args: List<String>, vararg entry: Map.Entry<String, (Queue<String>) -> Unit>) {
        val s: List<Pair<String, (Queue<String>) -> Unit>> = entry.map { it.toPair() };

        parseArguments(args, mapOf(*s.toTypedArray()))
    }

    /**
     * Allow for parsing arguments in a friendly manner
     */
    @JvmStatic
    fun parseArguments(args: List<String>, map: Map<String, (Queue<String>) -> Unit>) {
        val queue = LinkedList<String>(args);

        while (!queue.isEmpty()) {
            map[queue.pop()]?.invoke(queue)
        }

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
    fun parseArguments(args: List<String>): JavaArgumentArrayHelper {
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
    fun parseArguments(args: Array<String>): JavaArgumentArrayHelper {
        return JavaArgumentArrayHelper(args);
    }


}