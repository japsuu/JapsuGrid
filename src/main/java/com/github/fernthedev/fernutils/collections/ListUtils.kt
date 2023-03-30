package com.github.fernthedev.fernutils.collections

import com.google.common.collect.Lists

class ListUtils {

    companion object {
        @JvmStatic
        fun <T> splitList(list: List<T>, splitAmount: Int): List<List<T>> {
            return Lists.partition(list, list.size / splitAmount)
        }
    }

}