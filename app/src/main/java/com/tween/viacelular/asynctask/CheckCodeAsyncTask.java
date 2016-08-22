package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tween.viacelular.R;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.models.ConnectedAccount;
import com.tween.viacelular.models.User;
import com.tween.viacelular.models.UserHelper;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;
import io.realm.Realm;

public class CheckCodeAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Activity		activity;
	private Context			context;
	private String			code;
	private boolean			displayDialog	= true;
	private boolean			wasValidated	= true;

	//Modificación para permitir validar después de dejarlo pasar
	public CheckCodeAsyncTask(Context context, String code, boolean displayDialog)
	{
		this.context		= context;
		this.code			= code;
		this.displayDialog	= displayDialog;
		this.activity		= null;
	}

	protected void onPreExecute()
	{
		try
		{
			//Modificación para contemplar contexto sino se llamada desde una activity
			if(activity != null)
			{
				context = activity.getApplicationContext();
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

				progress = new MaterialDialog.Builder(context)
					.title(R.string.verify_dialog)
					.cancelable(false)
					.content(R.string.please_wait)
					.progress(true, 0)
					.show();
			}
		}
		catch(Exception e)
		{
			System.out.println("CheckCodeAsyncTask:onPreExecute - Exception: " + e);

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
			//Modificaciones para contemplar migración a Realm
			Realm realm						= Realm.getDefaultInstance();
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			User user						= realm.where(User.class).findFirst();
			JSONObject jsonSend				= new JSONObject();
			JSONObject jsonResult			= new JSONObject();
			String phone					= preferences.getString(User.KEY_PHONE, "");
			String gcmId					= preferences.getString(User.KEY_GCMID, "");

			if(user == null)
			{
				user = new User();
			}
			else
			{
				phone	= preferences.getString(User.KEY_PHONE, user.getPhone());
				gcmId	= preferences.getString(User.KEY_GCMID, user.getGcmId());
			}

			if(StringUtils.isEmpty(gcmId) || gcmId.equals(User.FAKE_GCMID_EMULATOR))
			{
				gcmId = FirebaseInstanceId.getInstance().getToken();

				if(StringUtils.isEmpty(gcmId))
				{
					gcmId = User.FAKE_GCMID_EMULATOR;
				}

				preferences.edit().putString(User.KEY_GCMID, gcmId).apply();
			}

			//Modificación para evitar duplicación de usuarios
			jsonSend.put(User.KEY_PHONE, phone);
			jsonSend.put(User.KEY_GCMID, gcmId);
			jsonSend.put(Common.KEY_CODE, code);

			//Agregado para enviar Sistema Operativo
			JSONObject info	= new JSONObject();
			info.put("os", "android");
			info.put("countryLanguage", Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry());
			jsonSend.put(Common.KEY_INFO, info);

			//Agregado para evitar enviar el put si el usuario se valido antes de que llegue el SMS
			if(user.getStatus() != User.STATUS_ACTIVE)
			{
				//Agregado para dejar pasar sin validar
				if(user.getStatus() == User.STATUS_INACTIVE)
				{
					setWasValidated(false);
				}

				jsonResult	= new JSONObject(ApiConnection.request(ApiConnection.USERS, context, ApiConnection.METHOD_PUT, preferences.getString(Common.KEY_TOKEN, ""), jsonSend.toString()));
				result		= ApiConnection.checkResponse(context, jsonResult);
			}
			else
			{
				result = ApiConnection.OK;
			}

			if(result.equals(ApiConnection.OK))
			{
				if(jsonResult.has(Common.KEY_CONTENT))
				{
					if(!jsonResult.isNull(Common.KEY_CONTENT))
					{
						JSONObject jsonData = jsonResult.getJSONObject(Common.KEY_CONTENT);

						if(jsonData != null)
						{
							User userParsed = UserHelper.parseJSON(jsonData, false, context);

							if(userParsed != null)
							{
								SharedPreferences.Editor editor = preferences.edit();

								if(StringUtils.isNotEmpty(userParsed.getUserId()))
								{
									editor.putString(User.KEY_API, userParsed.getUserId());
								}

								//Agregado para suspender el auto llamado al validar el código
								editor.putBoolean(Common.KEY_PREF_CALLME, false);
								editor.putBoolean(Common.KEY_PREF_LOGGED, true);
								editor.putBoolean(Common.KEY_PREF_CHECKED, true);
								//Agregado para reducir frencuencia para actualizar usuario
								editor.putLong(Common.KEY_PREF_TSUSER, System.currentTimeMillis());
								editor.apply();
								result = ApiConnection.OK;
							}
							else
							{
								result = context.getString(R.string.response_invalid);
							}
						}
						else
						{
							result = context.getString(R.string.response_invalid);
						}
					}
				}
			}

			//Agregado para prevenir pérdida de email
			user = realm.where(User.class).findFirst();

			if(user != null)
			{
				if(StringUtils.isEmpty(user.getEmail()))
				{
					ConnectedAccount connectedAccount = realm.where(ConnectedAccount.class).equalTo(Common.KEY_TYPE, ConnectedAccount.TYPE_GOOGLE).findFirst();

					if(connectedAccount != null)
					{
						realm.beginTransaction();
						user.setEmail(connectedAccount.getName());
						realm.commitTransaction();
					}
				}
			}
		}
		catch(JSONException e)
		{
			System.out.println("CheckCodeAsyncTask:doInBackground - JSONException: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			System.out.println("CheckCodeAsyncTask:doInBackground - Exception: " + e);

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

			if(result.equals(ApiConnection.OK))
			{
				//Se movió la llamada a api choreo para ejectuarla antes de validar, agregado para evitar redirección si pasó sin validar
				if(isWasValidated())
				{
					//Agregado para refrescar las suscripciones locales
					new UpdateUserAsyncTask(context, Common.BOOL_NO, false, "", true, false).execute();

					//Modificación para autosuscribir companies que tengan mensajes
					//BlockedActivity.modifySubscriptions(context, Common.BOOL_YES, true, "", false);
				}
			}
			else
			{
				Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
			}
		}
		catch(Exception e)
		{
			System.out.println("CheckCodeAsyncTask:onPostExecute - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		super.onPostExecute(result);
	}

	//Agregados para diferenciar validación normal de validación posterior al ingreso
	public boolean isWasValidated()
	{
		return wasValidated;
	}

	public void setWasValidated(final boolean wasValidated)
	{
		this.wasValidated = wasValidated;
	}
}
