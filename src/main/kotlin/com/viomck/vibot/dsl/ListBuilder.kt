package com.viomck.vibot.dsl

class ListBuilder<T> {
    val list = mutableListOf<T>()
    operator fun T.unaryPlus() = list.add(this)
}
