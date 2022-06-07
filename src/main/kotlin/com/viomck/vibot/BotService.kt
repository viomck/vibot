package com.viomck.vibot

import com.viomck.vibot.handler.onMessageCreate
import com.viomck.vibot.handler.onReactionAdd
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BotService {

    init {
        runBlocking {
            launch {
                start()
            }
        }
    }

    private suspend fun start() {
        val kord = Kord(System.getenv("BOT_TOKEN"))

        kord.on<MessageCreateEvent> { onMessageCreate(this) }
        kord.on<ReactionAddEvent> { onReactionAdd(this) }

        kord.login {
            @Suppress("OPT_IN_IS_NOT_ENABLED")
            @OptIn(PrivilegedIntent::class)
            intents += Intent.MessageContent
        }
    }

}