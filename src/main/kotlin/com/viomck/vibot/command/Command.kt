package com.viomck.vibot.command

import com.viomck.vibot.dsl.ListBuilder
import dev.kord.core.event.message.MessageCreateEvent

typealias CommandHandler = suspend (MessageCreateEvent, List<String>) -> Unit

data class Command(
    val aliases: List<String>,
    val adminOnly: Boolean,
    val handler: CommandHandler,
)

class CommandBuilder {
    private var aliases = mutableListOf<String>()
    private var adminOnly = false
    private var handler: CommandHandler? = null

    fun aliases(init: ListBuilder<String>.() -> Unit) {
        val builder = ListBuilder<String>()
        builder.init()
        aliases = builder.list
    }

    fun handler(handler: CommandHandler) {
        this.handler = handler
    }

    fun toCommand(): Command {
        return Command(aliases, adminOnly, handler!!)
    }
}

fun command(init: CommandBuilder.() -> Unit): Command {
    val builder = CommandBuilder()
    builder.init()
    return builder.toCommand()
}
