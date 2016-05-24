package com.tween.viacelular.models;

import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tween.viacelular.utils.Common;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by davidfigueroa on 29/4/16.
 * Con esta clase, el usuario, va a poder responder similar a un chat mediante xmpp
 */
public class MessageSender
{
	public AsyncTask<Void, Void, String> sendTask;
	public AtomicInteger ccsMsgId = new AtomicInteger();

	public void sendMessage(final Bundle data, final GoogleCloudMessaging gcm)
	{
		sendTask = new AsyncTask<Void, Void, String>()
		{
			@Override
			protected String doInBackground(Void... params)
			{
				String id = Integer.toString(ccsMsgId.incrementAndGet());

				try
				{
					System.out.println("messageid: " + id);
					gcm.send(Common.GCM_DEFAULTSENDERID+"@gcm.googleapis.com", id, data);
					System.out.println("After gcm.send successful.");
				}
				catch(Exception e)
				{
					System.out.println("MessageSender:sendMessage:doInBackground - Exception: " + e);

					if(Common.DEBUG)
					{
						e.printStackTrace();
					}
				}

				return "Message ID: "+id+ " Sent.";
			}

			@Override
			protected void onPostExecute(String result)
			{
				sendTask = null;
				System.out.println("onPostExecute: result: " + result);
			}
		};

		sendTask.execute(null, null, null);
	}
}