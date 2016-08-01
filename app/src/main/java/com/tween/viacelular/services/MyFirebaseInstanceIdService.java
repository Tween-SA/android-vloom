package com.tween.viacelular.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tween.viacelular.utils.Common;

/**
 * Created by davidfigueroa on 1/8/16.
 */
public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService
{
	private static final String FRIENDLY_ENGAGE_TOPIC = "global";

	/**
	 * The Application's current Instance ID token is no longer valid and thus a new one must be requested.
	 */
	@Override
	public void onTokenRefresh()
	{
		try
		{
			// If you need to handle the generation of a token, initially or after a refresh this is where you should do that.
			String token = FirebaseInstanceId.getInstance().getToken();
			System.out.println("FCM Token: " + token);

			// Once a token is generated, we subscribe to topic.
			FirebaseMessaging.getInstance().subscribeToTopic(FRIENDLY_ENGAGE_TOPIC);
		}
		catch(Exception e)
		{
			System.out.println("MyFirebaseInstanceIdService:onTokenRefresh - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}