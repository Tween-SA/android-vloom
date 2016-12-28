package com.tween.viacelular.services;

import android.app.Service;

/**
 * Created by David on 27/12/2016.
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