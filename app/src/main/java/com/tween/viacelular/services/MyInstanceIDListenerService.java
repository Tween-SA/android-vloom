package com.tween.viacelular.services;

import android.content.Intent;
import com.google.android.gms.iid.InstanceIDListenerService;
import com.tween.viacelular.utils.Common;

/**
 * Created by david.figueroa on 16/6/15.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService
{
	/**
	 * Called if InstanceID token is updated. This may occur if the security of the previous token had been compromised. This call is initiated by the InstanceID provider.
	 */
	@Override
	public void onTokenRefresh()
	{
		try
		{
			// Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
			Intent intent = new Intent(this, RegistrationIntentService.class);
			startService(intent);
		}
		catch(Exception e)
		{
			System.out.println("MyInstanceIDListenerService:onTokenRefresh - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}