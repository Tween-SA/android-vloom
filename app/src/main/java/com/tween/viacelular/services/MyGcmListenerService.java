package com.tween.viacelular.services;

import android.os.Bundle;
import com.google.android.gms.gcm.GcmListenerService;
import com.tween.viacelular.utils.Common;

/**
 * Created by david.figueroa on 16/6/15.
 */
public class MyGcmListenerService extends GcmListenerService
{
	/**
	 * Called when message is received.
	 *
	 * @param from SenderID of the sender.
	 * @param data Data bundle containing message data as key/value pairs. For Set of keys use data.keySet().
	 */
	@Override
	public void onMessageReceived(String from, Bundle data)
	{
		try
		{
			//Se deja de usar esta clase para capturar push con Firebase
			String bundle = "";
			if(data != null)
			{
				bundle = data.toString();
			}

			if(Common.DEBUG)
			{
				System.out.println("Bundle: "+bundle);
				System.out.println("From: " + from);
			}
		}
		catch(Exception e)
		{
			System.out.println("MyGcmListenerService:onMessageReceived - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}