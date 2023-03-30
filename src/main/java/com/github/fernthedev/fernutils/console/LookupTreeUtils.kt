package com.github.fernthedev.fernutils.console

import java.util.*
import javax.annotation.CheckReturnValue

/**
 * This is meant for parsing tree
 * arguments rather than a list
 * of arguments provided.
 */
object LookupTreeUtils {

    @JvmStatic
    fun <V: Any> parseArguments(args: Array<String>, vararg pair: Pair<String, (Queue<String>) -> V>, defaultArg: (Queue<String>) -> V = {Any() as V}) : V {
        return parseArguments(args.asList(), mapOf(*pair), defaultArg)
    }

    @JvmStatic
    fun <V: Any> parseArguments(args: Array<String>, map: Map<String, (Queue<String>) -> V>, defaultArg: (Queue<String>) -> V = {Any() as V}) : V {
        return parseArguments(args.asList(), map, defaultArg)
    }

    @JvmStatic
    fun <V: Any> parseArguments(args: List<String>, vararg pair: Pair<String, (Queue<String>) -> V>, defaultArg: (Queue<String>) -> V = {Any() as V}): V {
        return parseArguments(args, mapOf(*pair), defaultArg)
    }

    /**
     * Convenience method for Java 9 using Map.entry()
     *
     */
    @JvmStatic
    fun <V: Any> parseArguments(args: Array<String>, vararg entry: Map.Entry<String, (Queue<String>) -> V>, defaultArg: (Queue<String>) -> V = {Any() as V}): V {
        val s: List<Pair<String, (Queue<String>) -> V>> = entry.map { it.toPair() };

        return parseArguments(args, mapOf(*s.toTypedArray()), defaultArg)
    }

    /**
     * Convenience method for Java 9 using Map.entry()
     *
     */
    @JvmStatic
    fun <V: Any> parseArguments(args: List<String>, vararg entry: Map.Entry<String, (Queue<String>) -> V>, defaultArg: (Queue<String>) -> V = {Any() as V}): V {
        val s: List<Pair<String, (Queue<String>) -> V>> = entry.map { it.toPair() };

        return parseArguments(args, mapOf(*s.toTypedArray()), defaultArg)
    }

    /**
     * Allow for parsing arguments in a friendly manner
     */
    @JvmStatic
    fun <V: Any> parseArguments(args: List<String>, map: Map<String, (Queue<String>) -> V>, defaultArg: (Queue<String>) -> V = {Any() as V}): V {
        val queue = LinkedList<String>(args);

        for (s in args) {
            queue.pop()

            val handle = map[s]

            if (handle != null) {
                return handle(queue)
            }
        }

        return defaultArg.invoke(queue)
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
    fun <V: Any> parseArguments(args: List<String>): JavaLookupTreeHelper<V> {
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
    fun <V: Any> parseArguments(args: Array<String>): JavaLookupTreeHelper<V> {
        return JavaLookupTreeHelper(args);
    }

    /**
     * Allow for parsing arguments in a friendly manner
     * This is more specific to Java
     * in using a builder pattern
     * as the Kotlin code is not very
     * friendly in Java
     *
     * [clazz] Help to infer return type. Ignored otherwise
     *
     */
    @JvmStatic
    @CheckReturnValue
    fun <V: Any> parseArguments(args: List<String>, clazz: Class<V>): JavaLookupTreeHelper<V> {
        return parseArguments(args.toTypedArray())
    }

    /**
     * Allow for parsing arguments in a friendly manner
     * This is more specific to Java
     * in using a builder pattern
     * as the Kotlin code is not very
     * friendly in Java
     *
     * [clazz] Help to infer return type. Ignored otherwise
     *
     */
    @JvmStatic
    @CheckReturnValue
    fun <V: Any> parseArguments(args: Array<String>, clazz: Class<V>): JavaLookupTreeHelper<V> {
        return JavaLookupTreeHelper(args);
    }

    /**
     * Allow for parsing arguments in a friendly manner
     * This is more specific to Java
     * in using a builder pattern
     * as the Kotlin code is not very
     * friendly in Java
     *
     * [ignoredObject] Help to infer return type. Ignored otherwise
     */
    @JvmStatic
    @CheckReturnValue
    fun <V: Any> parseArguments(args: List<String>, ignoredObject: V): JavaLookupTreeHelper<V> {
        return parseArguments(args.toTypedArray())
    }

    /**
     * Allow for parsing arguments in a friendly manner
     * This is more specific to Java
     * in using a builder pattern
     * as the Kotlin code is not very
     * friendly in Java
     *
     * [ignoredObject] Help to infer return type. Ignored otherwise
     *
     */
    @JvmStatic
    @CheckReturnValue
    fun <V: Any> parseArguments(args: Array<String>, ignoredObject: V): JavaLookupTreeHelper<V> {
        return JavaLookupTreeHelper(args);
    }
}