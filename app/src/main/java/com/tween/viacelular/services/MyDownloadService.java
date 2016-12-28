package com.tween.viacelular.services;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by David on 27/12/2016.
 */
public class MyDownloadService extends MyBaseTaskService
{
	private static final String TAG = "Storage#DownloadService";

	/** Actions **/
	public static final String ACTION_DOWNLOAD = "action_download";
	public static final String DOWNLOAD_COMPLETED = "download_completed";
	public static final String DOWNLOAD_ERROR = "download_error";

	/** Extras **/
	public static final String EXTRA_DOWNLOAD_PATH = "extra_download_path";
	public static final String EXTRA_BYTES_DOWNLOADED = "extra_bytes_downloaded";

	private StorageReference mStorageRef;

	@Override
	public void onCreate()
	{
		super.onCreate();
		// Initialize Storage
		mStorageRef = FirebaseStorage.getInstance().getReference();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if(ACTION_DOWNLOAD.equals(intent.getAction()))
		{
			// Get the path to download from the intent
			String downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH);
			downloadFromPath(downloadPath);
		}

		return START_REDELIVER_INTENT;
	}

	private void downloadFromPath(final String downloadPath)
	{
		taskStarted();
		mStorageRef.child(downloadPath).getStream(new StreamDownloadTask.StreamProcessor()
		{
			@Override
			public void doInBackground(StreamDownloadTask.TaskSnapshot taskSnapshot, InputStream inputStream) throws IOException
			{
				inputStream.close();
			}
		})
		.addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>()
		{
			@Override
			public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot)
			{
				Log.d(TAG, "download:SUCCESS");
				Intent broadcast = new Intent(DOWNLOAD_COMPLETED);
				broadcast.putExtra(EXTRA_DOWNLOAD_PATH, downloadPath);
				broadcast.putExtra(EXTRA_BYTES_DOWNLOADED, taskSnapshot.getTotalByteCount());
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
				taskCompleted();
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception exception)
			{
				Intent broadcast = new Intent(DOWNLOAD_ERROR);
				broadcast.putExtra(EXTRA_DOWNLOAD_PATH, downloadPath);
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
				taskCompleted();
			}
		});
	}

	public static IntentFilter getIntentFilter()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(DOWNLOAD_COMPLETED);
		filter.addAction(DOWNLOAD_ERROR);
		return filter;
	}
}