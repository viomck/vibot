package com.viomck.vibot.command

import com.viomck.vibot.command.emoji.stealCommand
import com.viomck.vibot.command.misc.pingCommand

class CommandService {

    companion object {
        val instance = CommandService()
    }

    private val commandsByAlias = mutableMapOf<String, Command>()

    init {
        commands(
            // emoji
            pingCommand,
            // misc
            stealCommand,
        )
    }

    private fun commands(vararg commands: Command) {
        commands.forEach(::register)
    }

    private fun register(command: Command) {
        for (alias in command.aliases) {
            commandsByAlias[alias] = command
        }
    }

    fun getCommandByAlias(alias: String): Command? = commandsByAlias[alias]

}
