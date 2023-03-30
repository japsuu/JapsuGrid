package com.github.fernthedev.fernutils.strings;

public class StringUtil {

    private StringUtil() {}

    public static String lowerCamelCaseToUpperCamelCase(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String upperCamelCaseToLowerCamelCase(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String camelCaseToSnakeCase(String s) {
        return s.replaceAll("([a-z]+)([A-Z]+)", "$1_$2");
    }


}
