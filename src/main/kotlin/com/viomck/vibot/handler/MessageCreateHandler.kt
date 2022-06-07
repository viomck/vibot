package com.viomck.vibot.handler

import com.viomck.vibot.command.CommandService
import dev.kord.core.event.message.MessageCreateEvent

suspend fun onMessageCreate(event: MessageCreateEvent) {
    val author = event.message.author
    if (author == null || author.isBot) return

    val prefix = System.getenv("BOT_PREFIX") ?: "!"
    val content = event.message.content

    if (!content.startsWith(prefix) || content == prefix) return

    val args = content.substring(prefix.length).split(" ")
    val alias = args[0]

    val command = CommandService.instance.getCommandByAlias(alias) ?: return

    command.handler(event, args.subList(1, args.size))
}
