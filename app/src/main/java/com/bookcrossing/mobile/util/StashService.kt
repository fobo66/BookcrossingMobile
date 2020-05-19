/*
 *    Copyright  2019 Andrey Mukamolov
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bookcrossing.mobile.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.ui.bookpreview.BookActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class StashService : FirebaseMessagingService() {

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    val intent = Intent(this, BookActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.putExtra(EXTRA_KEY, remoteMessage.data["key"])

    val pendingIntent = PendingIntent.getActivity(this, 123456, intent, PendingIntent.FLAG_ONE_SHOT)
    val channelName = getString(R.string.stash_notification_channel)
    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    val notificationBuilder = NotificationCompat.Builder(this, channelName)
      .setSmallIcon(R.drawable.ic_book_black_24dp)
      .setContentTitle(
        resolveLocalizedNotificationText(
          remoteMessage.notification?.titleLocalizationKey
        )
      )
      .setContentText(
        resolveLocalizedNotificationText(
          remoteMessage.notification?.bodyLocalizationKey
        )
      )
      .setAutoCancel(true)
      .setSound(defaultSoundUri)
      .setContentIntent(pendingIntent)

    val notificationChannel = createNotificationChannel(channelName)

    with(NotificationManagerCompat.from(applicationContext)) {
      if (notificationChannel != null) {
        createNotificationChannel(notificationChannel)
      }
      notify(NOTIFICATION_ID, notificationBuilder.build())
    }
  }

  private fun resolveLocalizedNotificationText(localizationKey: String?): CharSequence? {
    return getString(resources.getIdentifier(localizationKey, "string", packageName))
  }

  private fun createNotificationChannel(channelName: String): NotificationChannel? {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

      val descriptionText = getString(R.string.stash_notification_channel_description)
      val importance = NotificationManager.IMPORTANCE_DEFAULT

      return NotificationChannel(CHANNEL_ID, channelName, importance).apply {
        description = descriptionText
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        enableVibration(false)
        enableLights(false)
      }
    }

    return null
  }

  companion object {
    private const val CHANNEL_ID: String = "stash"
    private const val NOTIFICATION_ID = 261998
  }
}