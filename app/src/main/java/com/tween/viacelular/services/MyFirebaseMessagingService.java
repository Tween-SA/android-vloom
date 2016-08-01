package com.tween.viacelular.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.DateUtils;
import com.tween.viacelular.utils.Utils;
import java.io.FileWriter;
import java.io.PrintWriter;

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
			Migration.getDB(getApplicationContext());
			// Handle data payload of FCM messages.
			System.out.println("FCM Message: " + remoteMessage.toString());
			System.out.println("FCM Message Id: " + remoteMessage.getMessageId());
			System.out.println("FCM Notification Message: " + remoteMessage.getNotification());
			System.out.println("FCM Data Message: " + remoteMessage.getData());
			FileWriter fichero	= null;
			PrintWriter pw		= null;

			try
			{
				fichero	= new FileWriter(Utils.path2Copy+"VloomPushTest.txt");
				pw		= new PrintWriter(fichero);
				pw.println(DateUtils.getDatePhone() + " - (thread) "+remoteMessage.getData());
			}
			catch(Exception d)
			{
				System.out.println("cargando fichero - Exception: " + d);
			}
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