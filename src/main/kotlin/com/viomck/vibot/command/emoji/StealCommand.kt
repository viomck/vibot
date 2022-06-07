package com.viomck.vibot.command.emoji

import com.viomck.vibot.command.command
import com.viomck.vibot.reaction.awaitReaction
import com.viomck.vibot.reaction.condEmojiWasAddedByVibot
import com.viomck.vibot.reaction.getCondReactorIs
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.createEmoji
import dev.kord.core.entity.Guild
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.Image
import io.ktor.client.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.selects.select
import java.lang.Integer.min

const val EmojiUrl = "https://cdn.discordapp.com/emojis/"
val NumberEmojis = listOf("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "\uD83D\uDD1F")
val EmojiMentionRegex = Regex("<(?<animated>a)?:(?<name>[A-z_]+):(?<id>\\d+)>")

val stealCommand = command {
    aliases {
        +"steal"
    }

    handler { e, args ->
        findEmoji(e, args) { emojiData ->
            if (emojiData == null) {
                e.message.channel.createMessage("No emoji found!")
                return@findEmoji
            }

            findDestGuild(e) { guild ->
                if (guild == null) {
                    e.message.channel.createMessage(
                        "No eligible destination server found.  To be eligible, I must be in the server and both " +
                            "you and me should have Manage Emoji (or Administrator) permissions."
                    )
                    return@findDestGuild
                }

                val ext = if (emojiData.animated) "gif" else "png"

                val emoji = guild.createEmoji(
                    emojiData.name,
                    Image.fromUrl(
                        HttpClient(),
                        "$EmojiUrl${emojiData.id}.${ext}?size=300&quality=lossless",
                    )
                )

                e.message.channel.createMessage(emoji.mention)
            }
        }
    }
}

private suspend fun findDestGuild(e: MessageCreateEvent, callback: suspend (Guild?) -> Unit) {
    val eligible = mutableListOf<Guild>()

    e.kord.guilds.filter {
        val perms = it.getMember(e.message.author!!.id).getPermissions()
        val selfPerms = it.getMember(e.kord.selfId).getPermissions()

        (perms.contains(Permission.ManageEmojisAndStickers) || perms.contains(Permission.Administrator)) &&
            (selfPerms.contains(Permission.ManageEmojisAndStickers) || perms.contains(Permission.Administrator))
    }.toList(eligible)

    when (eligible.size) {
        0 -> callback(null)
        1 -> callback(eligible[0])
        else -> findDestGuildFromReactions(e, eligible, callback)
    }
}

private suspend fun findDestGuildFromReactions(
    e: MessageCreateEvent,
    eligible: List<Guild>,
    callback: suspend (Guild?) -> Unit
) {
    val dm = e.message.author!!.fetchUser().getDmChannelOrNull()

    if (dm == null) {
        e.message.channel.createMessage("I can't DM you to have you select a guild!")
        callback(null)
        return
    }

    var message = "Pick a server to steal emoji to:\n"
    var i = 1

    for (guild in eligible) {
        message += "\n${NumberEmojis[i]} - ${guild.name}"

        if (i == 10) {
            message += "\n\nToo many eligible servers - capped at 10, sorry!"
            break
        }

        i++
    }

    val selectMessage = dm.createMessage(message)

    for (j in 1..min(10, eligible.size)) {
        selectMessage.addReaction(ReactionEmoji.Unicode(NumberEmojis[j]))
    }

    val dmRedirectMessage = e.message.channel.createMessage("Check your DMs to select a server")

    awaitReaction(selectMessage.id) {
        conditions { +::condEmojiWasAddedByVibot }
        callback {
            callback(eligible[NumberEmojis.indexOf(it.emoji.name) - 1])
            selectMessage.delete()
            dmRedirectMessage.delete()
            stopAwaiting()
        }
    }
}

private suspend fun findEmoji(e: MessageCreateEvent, args: List<String>, callback: suspend (EmojiData?) -> Unit) {
    tryToFindEmojiFromArgs(e, args) {
        if (it != null) callback(it)
        else tryToFindEmojiFromReply(e, callback)
    }
}

private suspend fun tryToFindEmojiFromArgs(e: MessageCreateEvent, args: List<String>, callback: suspend (EmojiData?) -> Unit) =
    findEmojiFromMessage(e, args, callback)

private suspend fun tryToFindEmojiFromReply(
    e: MessageCreateEvent,
    callback: suspend (EmojiData?) -> Unit
) {
    val repliedTo = e.message.referencedMessage
    if (repliedTo == null) callback(null)
    else findEmojiFromMessage(e, repliedTo.content.split(" "), callback)
}

private suspend fun findEmojiFromMessage(
    e: MessageCreateEvent,
    args: List<String>,
    callback: suspend (EmojiData?) -> Unit
) {
    val emojis = mutableListOf<EmojiData>()

    for (word in args) {
        val match = EmojiMentionRegex.matchEntire(word)

        if (match != null) {
            emojis.add(EmojiData.fromMatchResult(match))
        }
    }

    when (emojis.size) {
        0 -> callback(null)
        1 -> callback(emojis[0])
        else -> selectEmojiFromReactions(e, emojis, callback)
    }
}

private suspend fun selectEmojiFromReactions(
    e: MessageCreateEvent,
    emojis: List<EmojiData>,
    callback: suspend (EmojiData?) -> Unit
) {
    val extraContent = if (emojis.size > 10) "  Note: Too many emojis.  Capped off at #10." else ""

    // use fetchMessage to wait for send
    val selectMessage = e.message.channel.createMessage(
        "There are ${emojis.size} emojis in the selected message.  Please pick which one to use.$extraContent"
    ).fetchMessage()

    for (i in 1..min(10, emojis.size)) {
        selectMessage.addReaction(ReactionEmoji.Unicode(NumberEmojis[i]))
    }

    awaitReaction(selectMessage.id) {
        conditions {
            +::condEmojiWasAddedByVibot
            +getCondReactorIs(e.message.author!!.id)
        }
        callback {
            callback(emojis[NumberEmojis.indexOf(it.emoji.name) - 1])
            selectMessage.delete()
            stopAwaiting()
        }
    }
}

data class EmojiData(val name: String, val id: Snowflake, val animated: Boolean) {
    companion object {
        fun fromMatchResult(match: MatchResult) = EmojiData(
            match.groups["name"]!!.value,
            Snowflake(match.groups["id"]!!.value),
            match.groups["animated"] != null
        )
    }
}
