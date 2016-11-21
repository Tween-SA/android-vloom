package com.tween.viacelular.services;

import android.content.Context;
import android.os.AsyncTask;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tween.viacelular.asynctask.UpdateUserAsyncTask;
import com.tween.viacelular.utils.Common;

/**
 * Created by davidfigueroa on 1/8/16.
 */
public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService
{
	public static final String		FRIENDLY_ENGAGE_TOPIC	= "global";
	public static final String[]	TOPICS					= {"global"};
	public static final String		KEY						= "key";
	public static final String		TOPIC					= "topic";
	public static final String		SUBSCRIBE				= "subscribe";
	public static final String		UNSUBSCRIBE				= "unsubscribe";

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
			sendRegistrationToServer(token, getApplicationContext());
		}
		catch(Exception e)
		{
			System.out.println("MyFirebaseInstanceIdService:onTokenRefresh - Exception: "+e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void sendRegistrationToServer(final String token, Context context)
	{
		try
		{
			//Se envía siempre que se actualice el gcmId ya que en xmpp no podemos saber cuando un gcmId es inválido
			new UpdateUserAsyncTask(context, Common.BOOL_YES, false, token, false, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		catch(Exception e)
		{
			System.out.println("MyFirebaseInstanceIdService:sendRegistrationToServer - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}