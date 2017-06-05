package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.LandHelper;
import com.tween.viacelular.utils.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Manejador para actualización de países vía api
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar)
 */
public class CountryAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Context			context;
	private boolean			displayDialog	= false;

	public CountryAsyncTask(Context context, boolean displayDialog)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
	}

	protected void onPreExecute()
	{
		try
		{
			//Agregado para no mostrar dialogo por llamada en cadena
			if(displayDialog)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}

				progress = new MaterialDialog.Builder(context)
					.title(R.string.progress_dialog)
					.cancelable(false)
					.content(R.string.please_wait)
					.progress(true, 0)
					.show();
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "CountryAsyncTask:onPreExecute - Exception:", e);
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		String result = "";

		try
		{
			//Modificaciones para contemplar migración a Realm
			Realm realm						= Realm.getDefaultInstance();
			final RealmResults<Land> countries	= realm.where(Land.class).findAll();
			realm.executeTransaction(new Realm.Transaction()
			{
				@Override
				public void execute(Realm realm)
				{
					countries.deleteAllFromRealm();
				}
			});
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			JSONObject jsonResult			= new JSONObject(ApiConnection.getRequest(ApiConnection.COUNTRIES, context, preferences.getString(Common.KEY_TOKEN, ""), "", Common.TIMEOUT_API));
			result							= ApiConnection.checkResponse(context, jsonResult);
			boolean parseLocal				= true;
			JSONObject jsonData;
			JSONArray arrayKey;

			if(result.equals(ApiConnection.OK))
			{
				jsonData = jsonResult.getJSONObject(Common.KEY_CONTENT);

				if(jsonData != null)
				{
					//Renombre de key para contemplar nuevo estándar en api get countries
					if(jsonData.has(Common.KEY_DATA))
					{
						arrayKey = jsonData.getJSONArray(Common.KEY_DATA);

						if(arrayKey != null)
						{
							if(arrayKey.length() > 0)
							{
								result		= ApiConnection.OK;
								LandHelper.parseList(arrayKey, context);
								parseLocal	= false;
							}
						}
					}
				}
			}

			//Modificación para contemplar caso backup en el que no esté disponible la api aún
			if(parseLocal)
			{
				String json = ApiConnection.loadJSONFromAsset(context, "GETcountries.json");

				if(StringUtils.isNotEmpty(json))
				{
					jsonData	= new JSONObject(json);
					arrayKey	= jsonData.getJSONArray(Common.KEY_DATA);
					result		= ApiConnection.OK;
					LandHelper.parseList(arrayKey, context);
				}
				else
				{
					LandHelper.parseArray(context);
				}
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
			Utils.logError(context, "CountryAsyncTask:doInBackground - JSONException:", e);
		}
		catch(Exception e)
		{
			Utils.logError(context, "CountryAsyncTask:doInBackground - Exception:", e);
		}

		return result;
	}
}