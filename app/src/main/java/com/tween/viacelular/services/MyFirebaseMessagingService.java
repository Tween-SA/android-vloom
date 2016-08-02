package com.tween.viacelular.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.utils.Common;

/**
 * Created by davidfigueroa on 1/8/16.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService
{
	@Override
	public void onMessageReceived(RemoteMessage remoteMessage)
	{
		try
		{
			Migration.getDB(getApplicationContext(), Common.REALMDB_VERSION);
			// Handle data payload of FCM messages.
			System.out.println("FCM Message: " + remoteMessage.toString());
			System.out.println("FCM Message Id: " + remoteMessage.getMessageId());
			System.out.println("FCM Notification Message: " + remoteMessage.getNotification());
			System.out.println("FCM Data Message: " + remoteMessage.getData());
		}
		catch(Exception e)
		{
			System.out.println("MyFirebaseMessagingService:onMessageReceived - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}