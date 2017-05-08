package com.tween.viacelular.services;

import android.os.Bundle;
import com.google.android.gms.gcm.GcmListenerService;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.Utils;

/**
 * Servicio para captura de push notifications bajo GCM
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 16/06/15
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
			Utils.logError(null, "MyGcmListenerService:onMessageReceived - Exception:", e);
		}
	}
}