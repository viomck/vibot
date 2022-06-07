package com.viomck.vibot.command.misc

import com.viomck.vibot.command.command

val pingCommand = command {
    aliases {
        +"ping"
    }
    handler { e, _ ->
        e.message.channel.createMessage("Pong!")
    }
}
