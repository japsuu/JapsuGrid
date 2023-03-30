package com.github.fernthedev.fernutils.reflection

import java.math.BigDecimal


object ReflectionUtil {
    private val classParser: MutableMap<Class<*>, (String) -> Any> =
        HashMap()

    init {
        registerClassParser(Short::class.java) { s -> s.toShort()}
        registerClassParser(Array<String>::class.java) { s -> s.split(" ").toTypedArray()}
        registerClassParser(Float::class.java) { s -> s.toFloat()}
        registerClassParser(Int::class.java) { s -> s.toInt()}
        registerClassParser(Long::class.java) { s -> s.toLong()}
        registerClassParser(Double::class.java) { s -> s.toDouble()}
        registerClassParser(Boolean::class.java) { s -> s.toBoolean()}
        registerClassParser(BigDecimal::class.java) { s -> s.toBigDecimal()}
        registerClassParser(Byte::class.java) { s -> s.toByte()}
        registerClassParser(String::class.java) { s -> s}

    }

    @JvmStatic
    fun <T : Any> registerClassParser(
        clazz: Class<T>,
        parser: (String) -> T
    ) {
        classParser[clazz] = {s: String -> parser(s) } // Wrap to avoid type error
    }

    @JvmStatic
    fun <T : Any> getClassParser(
        clazz: Class<T>
    ): (String) -> T {
        return checkNotNull(classParser[clazz]) as (String) -> T
    }

    @JvmStatic
    fun <T : Any> parse(
        string: String,
        clazz: Class<T>
    ): T {
        if (clazz.isEnum) {
            val enumClass: Class<out Enum<*>> = clazz.asSubclass(Enum::class.java);

            return searchEnum(enumClass, string)!! as T
        }

        return checkNotNull(classParser[clazz])(string) as T
    }

    /**
     * Checks if the argument provided is valid
     * @param classes
     * @param arg
     * @return
     */
    @JvmStatic
    fun isValid(classes: List<Class<*>>, arg: Any?): Boolean {
        for (aClass in classes) {
            if (aClass.isInstance(arg)) {
                return true
            }
        }
        return false
    }


    /**
     * Checks if the argument provided is valid
     * @param classes
     * @param arg
     * @return
     */
    @JvmStatic
    fun isValid(classes: List<Class<*>>, arg: Class<*>): Boolean {
        for (aClass in classes) {
            if (aClass == arg || aClass.isAssignableFrom(arg)) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun <T : Enum<*>?> searchEnum(
        enumeration: Class<out T>,
        search: String?
    ): T? {
        for (each in enumeration.enumConstants) {
            if (each!!.name.equals(search, ignoreCase = true)) {
                return each
            }
        }
        return null
    }
}