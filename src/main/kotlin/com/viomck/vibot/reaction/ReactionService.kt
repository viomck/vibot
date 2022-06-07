package com.viomck.vibot.reaction

import com.viomck.vibot.dsl.ListBuilder
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.ReactionAddEvent
import kotlinx.coroutines.flow.firstOrNull

class ReactionService {

    companion object {
        val instance = ReactionService()
    }

    private val reactionAwaits = mutableMapOf<Snowflake, ReactionAwait>()

    suspend fun handle(e: ReactionAddEvent) {
        val reactionAwait = reactionAwaits[e.messageId] ?: return
        // not all conditions match
        if (!reactionAwait.conditions.all { it.invoke(e) }) return
        reactionAwait.callback.invoke(e)
    }

    fun addReactionAwait(reactionAwait: ReactionAwait) =
        reactionAwaits.put(reactionAwait.messageId, reactionAwait)

    fun removeReactionAwait(messageId: Snowflake) =
        reactionAwaits.remove(messageId)

}

class ReactionAwaitBuilder(private val messageId: Snowflake) {
    private var conditions = emptyList<Condition>()
    private var callback: ReactionCallback = { }

    suspend fun conditions(init: suspend ListBuilder<Condition>.() -> Unit) {
        val builder = ListBuilder<Condition>()
        builder.init()
        conditions = builder.list
    }

    fun callback(cb: ReactionCallback) = run { callback = cb }
    fun toReactionAwait(): ReactionAwait = ReactionAwait(messageId, conditions, callback)
    fun stopAwaiting() = ReactionService.instance.removeReactionAwait(messageId)
}

suspend fun awaitReaction(messageId: Snowflake, init: suspend ReactionAwaitBuilder.() -> Unit) {
    val builder = ReactionAwaitBuilder(messageId)
    builder.init()
    ReactionService.instance.addReactionAwait(builder.toReactionAwait())
}

fun getCondReactorIs(reactorId: Snowflake): Condition = { it.userId == reactorId }

suspend fun condEmojiWasAddedByVibot(e: ReactionAddEvent): Boolean =
    e.message.getReactors(e.emoji).firstOrNull { it.id == it.kord.selfId } != null

data class ReactionAwait(
    val messageId: Snowflake,
    val conditions: List<Condition>,
    val callback: ReactionCallback,
)

typealias ReactionCallback = suspend (ReactionAddEvent) -> Unit
typealias Condition = suspend (ReactionAddEvent) -> Boolean
