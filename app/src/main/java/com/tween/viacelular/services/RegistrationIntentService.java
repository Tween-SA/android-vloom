package com.tween.viacelular.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import com.appboy.Appboy;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.tween.viacelular.R;
import com.tween.viacelular.asynctask.UpdateUserAsyncTask;
import com.tween.viacelular.data.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.DateUtils;
import com.tween.viacelular.utils.StringUtils;

/**
 * Created by david.figueroa on 16/6/15.
 */
public class RegistrationIntentService extends IntentService
{
	private static final String		TAG			= "RegIntentService";
	private static final String[]	TOPICS		= {"global"};
	public static final String		KEY			= "key";
	public static final String		TOPIC		= "topic";
	public static final String		SUBSCRIBE	= "subscribe";
	public static final String		UNSUBSCRIBE	= "unsubscribe";

	public RegistrationIntentService()
	{
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		try
		{
			//Modificaciones para la implementación de xmpp
			if(intent != null)
			{
				String key		= intent.getStringExtra(KEY);
				String topic	= intent.getStringExtra(TOPIC);

				if(StringUtils.isNotEmpty(key))
				{
					switch(key)
					{
						case SUBSCRIBE:
							// subscribe to a topic
							subscribeToTopic(topic);
						break;

						case UNSUBSCRIBE:
							// unsubscribe to a topic
							//unsubscribeFromTopic(topic);
						break;

						default:
							// if key is specified, register with GCM
							registerGCM();
						break;
					}
				}
				else
				{
					registerGCM();
				}

				if(intent.getExtras() != null)
				{
					GoogleCloudMessaging gcm	= GoogleCloudMessaging.getInstance(this);
					String messageType			= "";

					if(gcm != null)
					{
						messageType = gcm.getMessageType(intent);
					}

					if(!intent.getExtras().isEmpty())
					{
						if(messageType.equals("send_error"))
						{
							System.out.println("Type error: "+intent.getExtras().toString());
						}
						else
						{
							if(messageType.equals("deleted_messages"))
							{
								System.out.println("Type deleted: "+intent.getExtras().toString());
							}
							else
							{
								if(messageType.equals("send_event"))
								{
									System.out.println("Type event: "+intent.getExtras().toString());
								}
								else
								{
									if(messageType.equals("gcm"))
									{
										System.out.println("Type message: "+intent.getExtras().toString());
										System.out.println("SM parameter: "+intent.getExtras().get("SM"));
									}
									else
									{
										System.out.println("Other type: "+intent.getExtras().toString());
										System.out.println("SM parameter: "+intent.getExtras().get("SM"));
									}
								}
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("RegistrationIntentService:onHandleIntent - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Registering with GCM and obtaining the gcm registration id
	 */
	private void registerGCM()
	{
		SharedPreferences sharedPreferences		= PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor preferences	= getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE).edit();

		try
		{
			//In the (unlikely) event that multiple refresh operations occur simultaneously, ensure that they are processed sequentially.
			synchronized(TAG)
			{
				//Initially this call goes out to the network to retrieve the token, subsequent calls are local.
				InstanceID instanceID	= InstanceID.getInstance(this);
				String token			= instanceID.getToken(getString(R.string.google_app_id), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

				if(Common.DEBUG)
				{
					System.out.println("GCM Registration Token: " + token);
				}

				//TODO: Implement this method to send any registration to your app's servers.
				sendRegistrationToServer(token);

				//Subscribe to topic channels
				subscribeTopics(token);

				//You should store a boolean that indicates whether the generated token has been sent to your server. If the boolean is false, send the token to your server,
				// otherwise your server should have already received the token.
				sharedPreferences.edit().putBoolean(MyGcmListenerService.SENT_TOKEN_TO_SERVER, true).apply();
				preferences.putString(User.KEY_GCMID, token);
				sharedPreferences.edit().apply();
				preferences.apply();

				//Agregado para registrar push en Appboy
				Appboy.getInstance(this).registerAppboyPushMessages(token);
			}
		}
		catch(Exception e)
		{
			System.out.println("RegistrationIntentService:registerGCM - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}

			//If an exception happens while fetching the new token or updating our registration data on a third-party server, this ensures that we'll attempt the update at a later time.
			sharedPreferences.edit().putBoolean(MyGcmListenerService.SENT_TOKEN_TO_SERVER, false).apply();
		}

		//Notify UI that registration has completed, so the progress indicator can be hidden.
		Intent registrationComplete = new Intent(MyGcmListenerService.REGISTRATION_COMPLETE);
		LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
	}

	/**
	 * Subscribe to a topic
	 * @param topic
	 */
	public void subscribeToTopic(final String topic)
	{
		if(Common.DEBUG)
		{
			System.out.println("RegistrationIntentService:subscribeToTopic: " + topic);
		}

		try
		{
			GcmPubSub pubSub		= GcmPubSub.getInstance(getApplicationContext());
			InstanceID instanceID	= InstanceID.getInstance(getApplicationContext());
			String token			= null;
			token					= instanceID.getToken(getString(R.string.google_app_id), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

			if(token != null)
			{
				pubSub.subscribe(token, "/topics/" + topic, null);
				SharedPreferences.Editor preferences	= getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE).edit();
				preferences.putString(User.KEY_GCMID, token);
				preferences.apply();
				System.out.println("Subscribed to topic: " + topic);
			}
			else
			{
				System.out.println("error: gcm registration id is null");
			}
		}
		catch(Exception e)
		{
			System.out.println("RegistrationIntentService:subscribeToTopic - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Unsubscribe to a topic
	 * @param topic
	 */
	public void unsubscribeFromTopic(final String topic)
	{
		if(Common.DEBUG)
		{
			System.out.println("RegistrationIntentService:unsubscribeFromTopic: " + topic);
		}

		try
		{
			GcmPubSub pubSub		= GcmPubSub.getInstance(getApplicationContext());
			InstanceID instanceID	= InstanceID.getInstance(getApplicationContext());
			String token			= null;
			token					= instanceID.getToken(getString(R.string.google_app_id), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

			if(token != null)
			{
				pubSub.unsubscribe(token, "");
				System.out.println("Unsubscribed from topic: " + topic);
			}
			else
			{
				System.out.println("error: gcm registration id is null");
			}
		}
		catch(Exception e)
		{
			System.out.println("RegistrationIntentService:unsubscribeFromTopic - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Persist registration to third-party servers.
	 * <p/>
	 * Modify this method to associate the user's GCM registration token with any server-side account maintained by your application.
	 *
	 * @param token The new token.
	 */
	private void sendRegistrationToServer(final String token)
	{
		try
		{
			//Agregado para limitar frecuencia de actualización
			SharedPreferences preferences	= getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			long tsUpated					= preferences.getLong(Common.KEY_PREF_TSUSER, System.currentTimeMillis());

			if(DateUtils.needUpdate(tsUpated, DateUtils.LOW_FREQUENCY))
			{
				UpdateUserAsyncTask task	= new UpdateUserAsyncTask(getApplicationContext(), Common.BOOL_YES, false, "", false);
				task.setToken(token);
				task.execute();
			}
		}
		catch(Exception e)
		{
			System.out.println("RegistrationIntentService:sendRegistrationToServer - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
	 *
	 * @param token GCM token
	 */
	// [START subscribe_topics]
	private void subscribeTopics(final String token)
	{
		try
		{
			for(String topic : TOPICS)
			{
				GcmPubSub pubSub = GcmPubSub.getInstance(this);
				pubSub.subscribe(token, "/topics/" + topic, null);
			}
		}
		catch(Exception e)
		{
			System.out.println("RegistrationIntentService:subscribeTopics - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}