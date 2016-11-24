package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.interfaces.CallBackListener;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.models.User;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmResults;

public class AttachAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog		progress;
	private Activity			activity;
	private boolean				displayDialog	= true;
	private CallBackListener	listener;
	private String				msgId;

	public AttachAsyncTask(Activity activity, boolean displayDialog, CallBackListener listener)
	{
		this.activity		= activity;
		this.displayDialog	= displayDialog;
		this.listener		= listener;
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
				System.out.println("Hay user task...");
				//JSONObject jsonResult	= new JSONObject(ApiConnection.request(ApiConnection.COMPANIES_BY_COUNTRY, activity, ApiConnection.METHOD_POST, preferences.getString(Common.KEY_TOKEN, ""), ""));
				result					= ApiConnection.OK;//ApiConnection.checkResponse(activity.getApplicationContext(), jsonResult);
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

			//Llamar al callback
			if(result.equals(ApiConnection.OK))
			{
				listener.callBack();
			}
			else
			{
				Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
			}

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