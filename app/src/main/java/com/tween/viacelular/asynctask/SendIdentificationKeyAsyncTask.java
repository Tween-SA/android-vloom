package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.User;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import io.realm.Realm;

public class SendIdentificationKeyAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Context			context;
	private boolean			displayDialog		= false;
	private String			identificationValue	= "";
	private String			companyId			= "";

	public SendIdentificationKeyAsyncTask(Context context, boolean displayDialog, String identificationValue, String companyId)
	{
		this.context				= context;
		this.displayDialog			= displayDialog;
		this.identificationValue	= identificationValue;
		this.companyId				= companyId;
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
					.title(R.string.landing_card_loading_header)
					.cancelable(false)
					.content(R.string.landing_card_loading_text)
					.progress(true, 0)
					.show();
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "RegisterPhoneAsyncTask:onPreExecute - Exception:", e);
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		String result		= "";

		try
		{
			//Modificaciones para contemplar migración a Realm
			Realm realm						= Realm.getDefaultInstance();
			final Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

			if(StringUtils.isAlphanumeric(identificationValue) && suscription != null)
			{
				User user = realm.where(User.class).findFirst();

				if(user != null)
				{
					String url						= ApiConnection.MODIFY_COMPANIES;
					url								= url.replace(User.KEY_API, user.getUserId());
					SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
					JSONArray jsonArray				= new JSONArray();
					JSONObject jsonObject			= new JSONObject();
					jsonObject.put(Suscription.KEY_API, suscription.getCompanyId());
					jsonObject.put(Suscription.KEY_SUSCRIBE, Common.BOOL_YES);
					jsonObject.put(Suscription.KEY_IDENTIFICATIONVALUE, identificationValue);
					jsonArray.put(jsonObject);

					JSONObject jsonResult	= new JSONObject(ApiConnection.request(url, context, ApiConnection.METHOD_PUT, preferences.getString(Common.KEY_TOKEN, ""), jsonArray.toString()));
					result					= ApiConnection.checkResponse(context, jsonResult);

					//TODO Posteriormente debe haber un if procesando la respuesta de la api cuando esté disponible
					result = ApiConnection.OK;
					realm.executeTransaction(new Realm.Transaction()
					{
						@Override
						public void execute(Realm realm)
						{
							suscription.setDataSent(Common.BOOL_YES);
							suscription.setIdentificationValue(identificationValue);
						}
					});
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
		catch(Exception e)
		{
			Utils.logError(context, "RegisterPhoneAsyncTask:doInBackground - Exception:", e);
		}

		return result;
	}
}