package com.bookcrossing.mobile.util;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class StashService extends FirebaseMessagingService {
  public StashService() {
  }

  @Override public void onMessageReceived(RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
  }
}
