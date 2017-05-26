package com.tween.viacelular.services;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Anterior identificación de token para implementación GCM ahora usamos FCM
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 16/06/15
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService
{
	/**
	 * Called if InstanceID token is updated. This may occur if the security of the previous token had been compromised. This call is initiated by the InstanceID provider.
	 */
	@Override
	public void onTokenRefresh()
	{
	}
}