package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.models.IspHelper;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.utils.Common;
import org.json.JSONObject;

/**
 * Created by davidfigueroa on 15/6/16.
 */
public class GetLocationByApiAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Context			context;
	private boolean			displayDialog	= false;
	private boolean			update			= false;

	public GetLocationByApiAsyncTask(final Context context, final boolean displayDialog, final boolean update)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.update			= update;
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

				progress = new MaterialDialog.Builder(context)
						.title(R.string.progress_dialog)
						.cancelable(false)
						.content(R.string.please_wait)
						.progress(true, 0)
						.show();
			}

			Migration.getDB(context);
		}
		catch(Exception e)
		{
			System.out.println("GetLocationByApiAsyncTask:onPreExecute - Exception: " + e);

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
			//TODO mejorar consulta
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			JSONObject jsonResult			= new JSONObject(ApiConnection.request(ApiConnection.IP_API, context, ApiConnection.METHOD_GET, preferences.getString(Common.KEY_TOKEN, ""), ""));
			result							= ApiConnection.checkResponse(context, jsonResult);

			if(result.equals(ApiConnection.OK))
			{
				IspHelper.parseJSON(jsonResult.getJSONObject(Common.KEY_CONTENT), context, update);
			}
		}
		catch(Exception e)
		{
			System.out.println("GetLocationByApiAsyncTask:doInBackground - Exception: " + e);

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

			if(!update)
			{
				CountryAsyncTask task = new CountryAsyncTask(context, false);
				task.execute();
			}
		}
		catch(Exception e)
		{
			System.out.println("GetLocationByApiAsyncTask:onPostExecute - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		super.onPostExecute(result);
	}
}
