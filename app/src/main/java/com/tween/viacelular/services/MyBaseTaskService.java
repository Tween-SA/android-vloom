package com.tween.viacelular.services;

import android.app.Service;


/**
 * Servicio base para descarga y subida a Firebase
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 27/12/16
 */
public abstract class MyBaseTaskService extends Service
{
	private int mNumTasks = 0;

	public void taskStarted()
	{
		changeNumberOfTasks(1);
	}

	public void taskCompleted()
	{
		changeNumberOfTasks(-1);
	}

	private synchronized void changeNumberOfTasks(int delta)
	{
		mNumTasks += delta;

		// If there are no tasks left, stop the service
		if(mNumTasks <= 0)
		{
			stopSelf();
		}
	}
}