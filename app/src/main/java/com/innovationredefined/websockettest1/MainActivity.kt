package com.innovationredefined.websockettest1

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.tinder.scarlet.*
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter
import com.tinder.scarlet.retry.ExponentialBackoffStrategy
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    private lateinit var _txtView: TextView
    private lateinit var _echoService: EchoService
    private val _echoServiceSubscriptions = arrayListOf<Disposable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _txtView = findViewById(R.id.txt)

        connectToMessagingService()

    }

    private fun connectToMessagingService() {

        val url1 = "wss://echo.websocket.org"
        val url2 = "ws://192.168.0.131/chat-service"

        //Configure client
        val scarlet = Scarlet.Builder()
            .webSocketFactory(
                OkHttpClient.Builder().build().newWebSocketFactory(url2)
            )
            .lifecycle(AndroidLifecycle.ofLifecycleOwnerForeground(application, this))
            .addMessageAdapterFactory(GsonMessageAdapter.Factory())
            .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
            .backoffStrategy(ExponentialBackoffStrategy(1000, 5000))
            .build()
        _echoService = scarlet.create()

        //Listen to events
        val echoServiceEventSubscription = _echoService.observeEvent()
            .observeOn(Schedulers.io())
            .subscribe({ event ->
                if (event is Event.OnWebSocket.Event<*>) {
                    when (event.event) {
                        is WebSocket.Event.OnConnectionOpened<*> -> {
                            handleConnectionOpen()
                        }
                        is WebSocket.Event.OnConnectionClosed, is WebSocket.Event.OnConnectionFailed -> {
                            handleConnectionClose()
                        }
                        else -> {

                        }
                    }
                }
            }, { e ->
                e.printStackTrace()
            })
        _echoServiceSubscriptions.add(echoServiceEventSubscription)

        //Listen to messages
        val echoServiceMessageSubscription =
            _echoService.observeMessage().observeOn(Schedulers.io()).subscribe({ message ->
                Log.v("testt", Gson().toJson(message))
            }, { e ->
                e.printStackTrace()
            })
        _echoServiceSubscriptions.add(echoServiceMessageSubscription)
    }

    private fun handleConnectionOpen() {

        //Authenticate
        _echoService.sendMessage(
            Message(
                type = "auth",
                text = "AUTH_TOKEN"
            )
        )

        runOnUiThread {
            _txtView.text = "\uD83D\uDEF0️ Connection Opened"

            //Start sending messages
            startMessaging()
        }

    }

    private fun handleConnectionClose() {
        runOnUiThread {
            _txtView.text = "\uD83D\uDEF0️ Connection Closed"
        }
    }

    private fun startMessaging() {
        object : CountDownTimer(3000000, 2000) {
            override fun onTick(millisUntilFinished: Long) {
                _echoService.sendMessage(
                    Message(
                        type = "msg",
                        senderUserProfileId = "38df556f-a8d5-471c-a2b7-d8849131e8cf",
                        recipientUserProfileId = "c8676e01-8bfe-4ef8-9e4c-945bfd33fca1",
                        text = "Hi"
                    )
                )

            }

            override fun onFinish() {
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        _echoServiceSubscriptions.forEach { subscription -> subscription.dispose() }
    }
}
