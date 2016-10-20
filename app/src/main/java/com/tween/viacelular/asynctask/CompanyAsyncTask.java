package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.utils.Common;
import org.json.JSONException;
import org.json.JSONObject;
import io.realm.Realm;

public class CompanyAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Context			context;
	private boolean			displayDialog	= true;
	private String			companyId		= "";
	private String			countryCode		= "";
	private int				flag			= 2;

	public CompanyAsyncTask(Context context, boolean displayDialog, String companyId, String countryCode)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.companyId		= companyId;
		this.countryCode	= countryCode;
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
		}
		catch(Exception e)
		{
			System.out.println("CompanyAsyncTask:onPreExecute - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		try
		{
			Suscription company;
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			JSONObject jsonResult			= new JSONObject(	ApiConnection.request(ApiConnection.COMPANIES + "/" + companyId, context, ApiConnection.METHOD_GET,
																preferences.getString(Common.KEY_TOKEN, ""), ""));
			String result					= ApiConnection.checkResponse(context, jsonResult);

			if(result.equals(ApiConnection.OK))
			{
				company = SuscriptionHelper.parseEntity(jsonResult.getJSONObject(Common.KEY_CONTENT), companyId, countryCode, context, false, getFlag());
			}
			else
			{
				company = SuscriptionHelper.parseEntity(null, companyId, countryCode, context, false, getFlag());
			}

			companyId = company.getCompanyId();

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
			System.out.println("CompanyAsyncTask:doInBackground - JSONException: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			System.out.println("CompanyAsyncTask:doInBackground - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return companyId;
	}

	public int getFlag()
	{
		return flag;
	}

	public void setFlag(final int flag)
	{
		this.flag = flag;
	}
}
