package com.bookcrossing.mobile.util

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bookcrossing.mobile.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class StashService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notification = NotificationCompat.Builder(this, getString(R.string.stash_notification_channel))
                .setContentTitle(remoteMessage.notification?.title)
                .setContentText(remoteMessage.notification?.body)
                .build()

        val manager = NotificationManagerCompat.from(applicationContext)
        manager.notify(0, notification)
    }
}
