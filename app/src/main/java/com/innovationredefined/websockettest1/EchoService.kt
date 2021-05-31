package com.innovationredefined.websockettest1

import com.tinder.scarlet.Event
import com.tinder.scarlet.State
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable

interface EchoService {
    @Receive
    fun observeState(): Flowable<State>

    @Receive
    fun observeEvent(): Flowable<Event>

    @Receive
    fun observeText(): Flowable<String>

    @Send
    fun sendText(message: String): Boolean

    @Send
    fun sendMessage(message: Message): Boolean

    @Receive
    fun observeMessage(): Flowable<Message>

    @Send
    fun sendBinary(byteArray: ByteArray): Boolean

}
