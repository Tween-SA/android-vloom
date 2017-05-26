package com.tween.viacelular.services;

import android.app.IntentService;
import android.content.Intent;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tween.viacelular.utils.Utils;

/**
 * Servicio para registar dispositivo al servicio de push notifications
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 16/06/15
 */
public class RegistrationIntentService extends IntentService
{
	private static final String		TAG			= "RegIntentService";
	public static final String		KEY			= "key";
	public static final String		TOPIC		= "topic";

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
				System.out.println("Key: "+key+" topic: "+topic);

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
			Utils.logError(null, "RegistrationIntentService:onHandleIntent - Exception:", e);
		}
	}
}