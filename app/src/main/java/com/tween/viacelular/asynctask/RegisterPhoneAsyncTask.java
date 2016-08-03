package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.CodeActivity;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.models.ConnectedAccount;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.User;
import com.tween.viacelular.models.UserHelper;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;
import io.realm.Realm;
import io.realm.RealmResults;

public class RegisterPhoneAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Activity		activity;
	private String			phone;
	private boolean			needRedirect	= true;

	public RegisterPhoneAsyncTask(Activity activity, String phone)
	{
		this.activity	= activity;
		this.phone		= phone;
	}

	public RegisterPhoneAsyncTask(Activity activity, String phone, boolean needRedirect)
	{
		this.activity		= activity;
		this.phone			= phone;
		this.needRedirect	= needRedirect;
	}

	protected void onPreExecute()
	{
		try
		{
			if(needRedirect)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}

				progress = new MaterialDialog.Builder(activity)
					.title(R.string.register_dialog)
					.cancelable(false)
					.content(R.string.please_wait)
					.progress(true, 0)
					.show();
			}
		}
		catch(Exception e)
		{
			System.out.println("RegisterPhoneAsyncTask - Exception: " + e);

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
			//Modificación para contemplar migración a Realm
			SharedPreferences preferences	= activity.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			Realm realm						= Realm.getDefaultInstance();
			RealmResults<User> users		= realm.where(User.class).findAll();
			realm.beginTransaction();
			users.deleteAllFromRealm();
			realm.commitTransaction();

			//Se quitó lo referido a isp para obtener desde clase sin consultar a la db
			JSONObject jsonSend		= new JSONObject();
			JSONObject info			= new JSONObject();
			JSONObject jsonResult	= new JSONObject();
			String email			= preferences.getString(User.KEY_EMAIL, "");

			if(StringUtils.isEmpty(email))
			{
				ConnectedAccount connectedAccount = realm.where(ConnectedAccount.class).equalTo(Common.KEY_TYPE, ConnectedAccount.TYPE_GOOGLE).findFirst();

				if(connectedAccount != null)
				{
					email = connectedAccount.getName();
				}
			}

			if(StringUtils.isNotEmpty(email))
			{
				info.put(User.KEY_EMAIL, email);
			}

			if(StringUtils.isNotEmpty(preferences.getString(Land.KEY_API, "")))
			{
				info.put(Land.KEY_API, preferences.getString(Land.KEY_API, ""));
			}

			//Modificado para obtener operadora sin consultar en la db
			if(StringUtils.isNotEmpty(Utils.getCarrierName(activity.getApplicationContext())))
			{
				info.put(User.KEY_CARRIER, Utils.getCarrierName(activity.getApplicationContext()));
			}

			//Agregado para enviar Sistema Operativo
			info.put("os", "android");
			info.put("countryLanguage", Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry());

			//TODO Probablemente en algún momento sea necesario agregar la info del device del usuario
			jsonSend.put(User.KEY_PHONE, phone);

			//Modificación para incoporar api de llamada desde esta misma AsyncTask
			if(needRedirect)
			{
				jsonSend.put(User.KEY_GCMID, preferences.getString(User.KEY_GCMID, User.FAKE_GCMID_EMULATOR));
				jsonSend.put(Common.KEY_INFO, info);

				jsonResult = new JSONObject(ApiConnection.request(ApiConnection.USERS, activity, ApiConnection.METHOD_POST, preferences.getString(Common.KEY_TOKEN, ""), jsonSend.toString()));
			}
			else
			{
				jsonResult = new JSONObject(ApiConnection.request(ApiConnection.CALLME, activity, ApiConnection.METHOD_POST, preferences.getString(Common.KEY_TOKEN, ""), jsonSend.toString()));
			}

			result = ApiConnection.checkResponse(activity.getApplicationContext(), jsonResult);

			if(result.equals(ApiConnection.OK) && needRedirect)
			{
				JSONObject jsonData = jsonResult.getJSONObject(Common.KEY_CONTENT);

				if(jsonData != null)
				{
					User userParsed = UserHelper.parseJSON(jsonData, false, null);

					if(userParsed != null)
					{
						SharedPreferences.Editor editor = preferences.edit();
						editor.putString(User.KEY_PHONE, phone);

						if(StringUtils.isNotEmpty(userParsed.getUserId()))
						{
							editor.putString(User.KEY_API, userParsed.getUserId());
						}

						editor.putBoolean(Common.KEY_PREF_LOGGED, true);
						editor.putBoolean(Common.KEY_PREF_CHECKED, false);
						//Agregado para resetear el contador de llamadas
						editor.putBoolean(Common.KEY_PREF_CALLME, true);
						editor.putInt(Common.KEY_PREF_CALLME_TIMES, 0);
						editor.apply();
						result = ApiConnection.OK;
					}
					else
					{
						if(needRedirect)
						{
							result = activity.getString(R.string.response_invalid);
						}
					}
				}
				else
				{
					result = activity.getString(R.string.response_invalid);
				}
			}
		}
		catch(JSONException e)
		{
			System.out.println("RegisterPhoneAsyncTask - JSONException: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			System.out.println("RegisterPhoneAsyncTask - Exception: " + e);

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
			//Modificación para no mostrar error al no conectar api de tts y evitar solicitar llamadas innecesarias
			if(needRedirect)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}

				if(result.equals(ApiConnection.OK))
				{
					Intent intent = new Intent(activity.getApplicationContext(), CodeActivity.class);
					activity.startActivity(intent);
					activity.finish();
				}
				else
				{
					Toast.makeText(activity.getApplicationContext(), result, Toast.LENGTH_LONG).show();
				}
			}
			else
			{
				//Agregado para limitar a dos llamdas únicamente
				SharedPreferences preferences	= activity.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor	= preferences.edit();
				int times						= preferences.getInt(Common.KEY_PREF_CALLME_TIMES, 0);
				editor.putInt(Common.KEY_PREF_CALLME_TIMES, times + 1);

				if(times >= 2)
				{
					editor.putBoolean(Common.KEY_PREF_CALLME, false);
				}

				editor.apply();
			}
		}
		catch(Exception e)
		{
			System.out.println("RegisterPhoneAsyncTask - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		super.onPostExecute(result);
	}
}