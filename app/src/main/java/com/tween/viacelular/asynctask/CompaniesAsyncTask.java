package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import io.realm.Realm;
import io.realm.RealmResults;

public class CompaniesAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Activity		activity;
	private boolean			displayDialog	= true;

	public CompaniesAsyncTask(Activity activity, boolean displayDialog)
	{
		this.activity		= activity;
		this.displayDialog	= displayDialog;
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
			System.out.println("CompaniesAsyncTask:onPreExecute - Exception: " + e);
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
			RealmResults<Suscription> results	= realm.where(Suscription.class).findAll();
			realm.beginTransaction();
			results.deleteAllFromRealm();
			realm.commitTransaction();
			String country						= "";
			User user							= realm.where(User.class).findFirst();

			if(user != null)
			{
				if(StringUtils.isNotEmpty(user.getCountryCode()))
				{
					country = user.getCountryCode();
				}
			}

			if(StringUtils.isEmpty(country))
			{
				if(StringUtils.isNotEmpty(preferences.getString(Land.KEY_API, "")))
				{
					country = preferences.getString(Land.KEY_API, "");
				}
				else
				{
					country = Land.DEFAULT_VALUE;
				}
			}

			SharedPreferences.Editor editor	= preferences.edit();
			editor.putString(Land.KEY_API, country);
			editor.apply();
			JSONObject jsonResult	= new JSONObject(	ApiConnection.request(ApiConnection.COMPANIES_BY_COUNTRY + "=" + country, activity, ApiConnection.METHOD_GET,
														preferences.getString(Common.KEY_TOKEN, ""), ""));
			result					= ApiConnection.checkResponse(activity.getApplicationContext(), jsonResult);

			if(result.equals(ApiConnection.OK))
			{
				SuscriptionHelper.parseList(jsonResult.getJSONArray(Common.KEY_CONTENT), activity.getApplicationContext(), false);
			}
			else
			{
				SuscriptionHelper.parseList(null, activity.getApplicationContext(), false);
			}

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
		}
		catch(JSONException e)
		{
			System.out.println("CompaniesAsyncTask:doInBackground - JSONException: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			System.out.println("CompaniesAsyncTask:doInBackground - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}
}