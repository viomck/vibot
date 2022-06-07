package com.viomck.vibot.handler

import com.viomck.vibot.reaction.ReactionService
import dev.kord.core.event.message.ReactionAddEvent

suspend fun onReactionAdd(e: ReactionAddEvent) {
    if (e.user.asUser().isBot) return
    ReactionService.instance.handle(e)
}
