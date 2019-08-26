package com.bookcrossing.mobile.util

import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.ui.bookpreview.BookActivity
import com.bookcrossing.mobile.util.Constants.EXTRA_KEY
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class StashService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val intent = Intent(this, BookActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(EXTRA_KEY, remoteMessage.data["key"])

        val pendingIntent = PendingIntent.getActivity(this, 123456, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = getString(R.string.stash_notification_channel)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_book_black_24dp)
                .setContentTitle(resolveLocalizedNotificationText(remoteMessage.notification?.titleLocalizationKey))
                .setContentText(resolveLocalizedNotificationText(remoteMessage.notification?.bodyLocalizationKey))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(applicationContext)


        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun resolveLocalizedNotificationText(localizationKey: String?): CharSequence? {
        return getString(resources.getIdentifier(localizationKey, "string", packageName))
    }
}
