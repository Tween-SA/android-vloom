package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.interfaces.CallBackListener;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.User;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import io.realm.Realm;

public class AttachAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog		progress;
	private Activity			activity;
	private boolean				displayDialog	= true;
	private CallBackListener	listener;
	private String				msgId;

	public AttachAsyncTask(Activity activity, boolean displayDialog, String msgId, CallBackListener listener)
	{
		this.activity		= activity;
		this.displayDialog	= displayDialog;
		this.listener		= listener;
		this.msgId			= msgId;
	}

	protected void onPreExecute()
	{
		try
		{
			if(displayDialog)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}

				progress = new MaterialDialog.Builder(activity)
					.title(R.string.progress_dialog)
					.cancelable(false)
					.content(R.string.please_wait)
					.progress(true, 0)
					.show();
			}
		}
		catch(Exception e)
		{
			System.out.println("AttachAsyncTask:onPreExecute - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		String result = "";

		try
		{
			//Modificaciones para contemplar migraciónd de db
			Realm realm							= Realm.getDefaultInstance();
			SharedPreferences preferences		= activity.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			User user							= realm.where(User.class).findFirst();

			if(user != null)
			{
				if(StringUtils.isNotEmpty(msgId))
				{
					Message message = realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();

					if(message != null)
					{
						//JSONObject jsonResult	= new JSONObject(ApiConnection.request(ApiConnection.COMPANIES_BY_COUNTRY, activity, ApiConnection.METHOD_POST, preferences.getString(Common.KEY_TOKEN, ""), ""));
						result					= ApiConnection.OK;//ApiConnection.checkResponse(activity.getApplicationContext(), jsonResult);
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("AttachAsyncTask:doInBackground - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}

	@Override
	protected void onPostExecute(String result)
	{
		try
		{
			if(displayDialog)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}
			}

			System.out.println("onPostExecute task... result: "+result);
			//Llamar al callback
			if(!result.equals(ApiConnection.OK))
			{
				Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
			}

			listener.callBack();
		}
		catch(Exception e)
		{
			System.out.println("AttachAsyncTask:onPostExecute - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		super.onPostExecute(result);
	}
}