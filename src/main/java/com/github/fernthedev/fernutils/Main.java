package com.github.fernthedev.fernutils;

import java.util.List;
import java.util.Map;

/**
 * Testing
 */
public class Main {

    public static void main(String[] args) {
        Settings settings = new Settings();

        Map<String, List<String>> map = settings.multiThread(true, true);

        Map<String, List<String>> mapStr = settings.singleThread(true, true);

        System.out.println(map + "\n"+ mapStr);
    }
}
